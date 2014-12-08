package org.iplantc.de.client.services.impl;

import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.GET;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.PATCH;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.POST;
import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.PUT;

import org.iplantc.de.client.models.DEProperties;
import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.apps.integration.AppTemplate;
import org.iplantc.de.client.models.apps.integration.AppTemplateAutoBeanFactory;
import org.iplantc.de.client.models.apps.integration.Argument;
import org.iplantc.de.client.models.apps.integration.ArgumentGroup;
import org.iplantc.de.client.models.apps.integration.ArgumentType;
import org.iplantc.de.client.models.apps.integration.ArgumentValidator;
import org.iplantc.de.client.models.apps.integration.DataSource;
import org.iplantc.de.client.models.apps.integration.DataSourceList;
import org.iplantc.de.client.models.apps.integration.FileInfoType;
import org.iplantc.de.client.models.apps.integration.FileInfoTypeList;
import org.iplantc.de.client.models.apps.integration.JobExecution;
import org.iplantc.de.client.models.apps.integration.SelectionItem;
import org.iplantc.de.client.models.apps.integration.SelectionItemGroup;
import org.iplantc.de.client.models.apps.refGenome.ReferenceGenome;
import org.iplantc.de.client.models.apps.refGenome.ReferenceGenomeList;
import org.iplantc.de.client.models.tool.Tool;
import org.iplantc.de.client.services.AppMetadataServiceFacade;
import org.iplantc.de.client.services.AppTemplateServices;
import org.iplantc.de.client.services.converters.AppTemplateCallbackConverter;
import org.iplantc.de.client.util.AppTemplateUtils;
import org.iplantc.de.client.util.JsonUtil;
import org.iplantc.de.shared.services.DiscEnvApiService;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;

import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

public class AppTemplateServicesImpl implements AppTemplateServices, AppMetadataServiceFacade {

    private final String APPS = "org.iplantc.services.apps";
    private final String ARG_PREVIEW = "org.iplantc.services.apps.argPreview";
    private final String DATA_SOURCES = "org.iplantc.services.apps.elements.dataSources";
    private final String FILE_INFO_TYPES = "org.iplantc.services.apps.elements.infoTypes";
    private final String REFERENCE_GENOMES = "org.iplantc.services.referenceGenomes";
    private final String ANALYSES = "org.iplantc.services.analyses";

    private final AppTemplateAutoBeanFactory factory;
    private static final Queue<AsyncCallback<List<DataSource>>> dataSourceQueue = Lists.newLinkedList();
    private static final Queue<AsyncCallback<List<FileInfoType>>> fileInfoTypeQueue = Lists.newLinkedList();
    private static final Queue<AsyncCallback<List<ReferenceGenome>>> refGenQueue = Lists.newLinkedList();
    private final List<DataSource> dataSourceList = Lists.newArrayList();
    private final List<FileInfoType> fileInfoTypeList = Lists.newArrayList();

    private final List<ReferenceGenome> refGenList = Lists.newArrayList();
    private final DiscEnvApiService deServiceFacade;
    private final DEProperties deProperties;

    private static final Logger LOG = Logger.getLogger(AppTemplateServicesImpl.class.getName());

    @Inject
    public AppTemplateServicesImpl(final DiscEnvApiService deServiceFacade,
                                   final DEProperties deProperties,
                                   final AppTemplateAutoBeanFactory factory) {
        this.deServiceFacade = deServiceFacade;
        this.deProperties = deProperties;
        this.factory = factory;
    }

    @Override
    public void cmdLinePreview(AppTemplate at, AsyncCallback<String> callback) {
        String address = ARG_PREVIEW;
        AppTemplate cleaned = doCmdLinePreviewCleanup(at);
        // SS: Service wont accept string values for dates
        cleaned.setEditedDate(null);
        Splittable split = appTemplateToSplittable(cleaned);
        String payload = split.getPayload();
        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, payload);
        deServiceFacade.getServiceData(wrapper, callback);
    }

    @Override
    public void getAppTemplate(HasId appId, AsyncCallback<AppTemplate> callback) {
        String address = APPS + "/" + appId.getId();
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));
    }

    @Override
    public AppTemplateAutoBeanFactory getAppTemplateFactory() {
        return factory;
    }

    @Override
    public void getAppTemplateForEdit(HasId appId, AsyncCallback<AppTemplate> callback) {
        String address = APPS + "/" + appId.getId() + "/ui";
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));
    }

    @Override
    public void getAppTemplatePreview(AppTemplate at, AsyncCallback<AppTemplate> callback) {
        String address = deProperties.getUnproctedMuleServiceBaseUrl() + "preview-template";
        Splittable split = appTemplateToSplittable(at);
        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, split.getPayload());
        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));
    }

    @Override
    public void getDataSources(AsyncCallback<List<DataSource>> callback) {
        if (!dataSourceList.isEmpty()) {
            callback.onSuccess(dataSourceList);
        } else {
            enqueueDataSourceCallback(callback);
        }
    }

    @Override
    public void getFileInfoTypes(AsyncCallback<List<FileInfoType>> callback) {
        if (!fileInfoTypeList.isEmpty()) {
            callback.onSuccess(fileInfoTypeList);
        } else {
            enqueueFileInfoTypeCallback(callback);
        }
    }

    @Override
    public void getReferenceGenomes(AsyncCallback<List<ReferenceGenome>> callback) {
        if (!refGenList.isEmpty()) {
            callback.onSuccess(refGenList);
        } else {
            enqueueRefGenomeCallback(callback);
        }
    }

    @Override
    public void launchAnalysis(AppTemplate at, JobExecution je, AsyncCallback<String> callback) {
        String address = ANALYSES;
        Splittable assembledPayload = doAssembleLaunchAnalysisPayload(at, je);
        LOG.info("LaunchAnalysis Json:\n" + JsonUtil.prettyPrint(assembledPayload));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, assembledPayload.getPayload());
        deServiceFacade.getServiceData(wrapper, callback);
    }

    @Override
    public void rerunAnalysis(HasId analysisId, AsyncCallback<AppTemplate> callback) {
        String address = ANALYSES + "/" + analysisId.getId() + "/relaunch-info";

        ServiceCallWrapper wrapper = new ServiceCallWrapper(GET, address);

        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));
    }

    @Override
    public void saveAndPublishAppTemplate(AppTemplate at, AsyncCallback<AppTemplate> callback) {
        String address = APPS + "/" + at.getId();
        Splittable split = appTemplateToSplittable(at);
        ServiceCallWrapper wrapper = new ServiceCallWrapper(PUT, address, split.getPayload());
        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));
    }

    @Override
    public void createAppTemplate(AppTemplate at, AsyncCallback<AppTemplate> callback) {
        String address = APPS;
        Splittable split = appTemplateToSplittable(at);
        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, split.getPayload());
        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));

    }

    @Override
    public void updateAppLabels(AppTemplate at, AsyncCallback<AppTemplate> callback) {
        String address = APPS +  "/" + at.getId();
        Splittable split = appTemplateToSplittable(at);
        ServiceCallWrapper wrapper = new ServiceCallWrapper(PATCH, address, split.getPayload());
        deServiceFacade.getServiceData(wrapper, new AppTemplateCallbackConverter(factory, callback));

    }

    Splittable appTemplateToSplittable(AppTemplate at) {
        AutoBean<AppTemplate> autoBean = AutoBeanUtils.getAutoBean(cleanTempIdFromValidators(at));
        Splittable ret = AutoBeanCodex.encode(autoBean);
        if (at.getTools() != null && at.getTools().size() > 0) {
            Splittable tools = StringQuoter.createIndexed();
            for (Tool t : at.getTools()) {
                AutoBean<Tool> toolBean = AutoBeanUtils.getAutoBean(t);
                if (toolBean != null) {
                    Splittable sp = AutoBeanCodex.encode(toolBean);
                    sp.assign(tools, tools.size());
                }
            }
            tools.assign(ret, "tools");

        }
        // JDS Convert Argument.getValue() which contain any selected/checked *Selection types to only
        // contain their value.
        // SS clear temp id for the validators if any
        for (ArgumentGroup ag : at.getArgumentGroups()) {
            for (Argument arg : ag.getArguments()) {
                if (arg.getType().equals(ArgumentType.TreeSelection)) {
                    if ((arg.getSelectionItems() != null) && (arg.getSelectionItems().size() == 1)) {
                        SelectionItemGroup sig = AppTemplateUtils.selectionItemToSelectionItemGroup(arg.getSelectionItems().get(0));
                        Splittable split = AppTemplateUtils.getSelectedTreeItemsAsSplittable(sig);
                        arg.setValue(split);
                    }
                }

            }
        }
        LOG.info("template from bean-->" + ret.getPayload() + "");
        return ret;
    }

    AppTemplate cleanTempIdFromValidators(AppTemplate at) {
        for (ArgumentGroup ag : at.getArgumentGroups()) {
            for (Argument arg : ag.getArguments()) {
                if (arg.getValidators() != null && arg.getValidators().size() > 0) {
                    for (ArgumentValidator av : arg.getValidators()) {
                        String id = av.getId();
                        if (id != null && id.contains("TEMP_ID_")) {
                            av.setId(null);
                        }
                    }
                }
            }
        }

        return at;
    }

    Splittable doAssembleLaunchAnalysisPayload(AppTemplate at, JobExecution je) {
        Splittable assembledPayload = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(je));
        Splittable configSplit = StringQuoter.createSplittable();
        for (ArgumentGroup ag : at.getArgumentGroups()) {
            for (Argument arg : ag.getArguments()) {
                Splittable value = arg.getValue();
                final boolean diskResourceArgumentType = AppTemplateUtils.isDiskResourceArgumentType(arg.getType());
                final boolean simpleSelectionArgumentType = AppTemplateUtils.isSimpleSelectionArgumentType(arg.getType());

                if (value == null) {
                    continue;
                }

                if (simpleSelectionArgumentType) {
                    value.assign(configSplit, arg.getId());
                } else if (diskResourceArgumentType
                               && !arg.getType().equals(ArgumentType.MultiFileSelector)) {
                    value.get("path").assign(configSplit, arg.getId());
                } else if (arg.getType().equals(ArgumentType.MultiFileSelector)
                               && value.isIndexed()) {
                    value.assign(configSplit, arg.getId());
                } else if (arg.getType().equals(ArgumentType.TreeSelection)
                               && (arg.getSelectionItems() != null)
                               && (arg.getSelectionItems().size() == 1)) {
                    pruneArgumentsFromSelectionItemGroups(value);
                    pruneSelectedItemsWithNoFlagsNorValues(value).assign(configSplit, arg.getId());
                } else {
                    value.assign(configSplit, arg.getId());
                }
            }
        }
        configSplit.assign(assembledPayload, "config");
        return assembledPayload;
    }

    /**
     * @param value and indexed splittable.
     * @return an indexed splittable which contains {@link SelectionItem}s whose {@code "name"} or
     * {@code "value"} JSON keys with non-null values.
     */
    Splittable pruneSelectedItemsWithNoFlagsNorValues(final Splittable value){
        Splittable ret = StringQuoter.createIndexed();
        for(int i = 0; i < value.size(); i++){
            final Splittable splittable = value.get(i);

            if(!splittable.isUndefined("value")
                    && !Strings.isNullOrEmpty(splittable.get("value").asString())){
                // If the item has a non-null value, add it
                splittable.assign(ret, ret.size());
            } else if (!splittable.isUndefined(SelectionItem.ARGUMENT_OPTION_KEY)
                         && !Strings.isNullOrEmpty(splittable.get(SelectionItem.ARGUMENT_OPTION_KEY).asString())){
                // If the item has a non-null argument option, add it
                splittable.assign(ret, ret.size());
            }
        }

        return ret;
    }

    /**
     * Nulls out the value associated with {@link SelectionItemGroup#ARGUMENTS_KEY} JSON key in the
     * children of the given indexed splittable.
     *
     * @param value and indexed splittable
     */
    void pruneArgumentsFromSelectionItemGroups(final Splittable value){
        for(int i = 0; i < value.size(); i++){
            final Splittable splittable = value.get(i);
            if(splittable.isUndefined(SelectionItemGroup.ARGUMENTS_KEY)){
                // If the key is undefined, continue
                continue;
            }
            // Remove arguments from selectionItemGroup
            Splittable.NULL.assign(splittable, SelectionItemGroup.ARGUMENTS_KEY);
        }
    }

    AppTemplate doCmdLinePreviewCleanup(AppTemplate templateToClean) {
        AppTemplate copy = AppTemplateUtils.copyAppTemplate(templateToClean);
        // JDS Transform any Argument's value which contains a full SelectionItem obj to the
        // SelectionItem's value
        for (ArgumentGroup ag : copy.getArgumentGroups()) {
            for (Argument arg : ag.getArguments()) {
                if (AppTemplateUtils.isSimpleSelectionArgumentType(arg.getType())) {

                    if ((arg.getValue() != null) && arg.getValue().isKeyed() && !arg.getValue().isUndefined("value")) {
                        arg.setValue(arg.getValue().get("value"));
                    } else {
                        arg.setValue(null);
                    }
                } else if (arg.getType().equals(ArgumentType.TreeSelection)) {
                    if ((arg.getSelectionItems() != null) && (arg.getSelectionItems().size() == 1)) {
                        SelectionItemGroup sig = AppTemplateUtils.selectionItemToSelectionItemGroup(arg.getSelectionItems().get(0));
                        List<SelectionItem> siList = AppTemplateUtils.getSelectedTreeItems(sig);
                        String retVal = "";
                        for (SelectionItem si : siList) {
                            if (si.getValue() != null) {
                                retVal += si.getValue() + " ";
                            }
                        }
                        arg.setValue(StringQuoter.create(retVal.trim()));
                    }
                } else if (arg.getType().equals(ArgumentType.EnvironmentVariable)) {
                    // Exclude environment variables from the command line
                    arg.setValue(null);
                    arg.setName("");
                } else if (AppTemplateUtils.isDiskResourceOutputType(arg.getType())) {
                    if (arg.getFileParameters().isImplicit()) {
                        arg.setValue(null);
                        arg.setName("");
                    }
                }
            }
        }

        return copy;
    }

    private void enqueueDataSourceCallback(final AsyncCallback<List<DataSource>> callback) {
        if (dataSourceQueue.isEmpty()) {
            String address = DATA_SOURCES;
            ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
            deServiceFacade.getServiceData(wrapper, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(String result) {
                    DataSourceList dsList = AutoBeanCodex.decode(factory, DataSourceList.class, result).as();
                    dataSourceList.clear();
                    dataSourceList.addAll(dsList.getDataSources());

                    while (!dataSourceQueue.isEmpty()) {
                        dataSourceQueue.remove().onSuccess(dataSourceList);
                    }
                }
            });

        }
        dataSourceQueue.add(callback);
    }

    private void enqueueFileInfoTypeCallback(final AsyncCallback<List<FileInfoType>> callback) {
        if (fileInfoTypeQueue.isEmpty()) {
            String address = FILE_INFO_TYPES;
            ServiceCallWrapper wrapper = new ServiceCallWrapper(address);

            deServiceFacade.getServiceData(wrapper, new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(String result) {
                    FileInfoTypeList fitListWrapper = AutoBeanCodex.decode(factory, FileInfoTypeList.class, result).as();

                    fileInfoTypeList.clear();
                    fileInfoTypeList.addAll(fitListWrapper.getFileInfoTypes());

                    while (!fileInfoTypeQueue.isEmpty()) {
                        fileInfoTypeQueue.remove().onSuccess(fileInfoTypeList);
                    }
                }
            });
        }
        fileInfoTypeQueue.add(callback);

    }

    private void enqueueRefGenomeCallback(final AsyncCallback<List<ReferenceGenome>> callback) {
        if (refGenQueue.isEmpty()) {
            String address = REFERENCE_GENOMES;
            ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
            deServiceFacade.getServiceData(wrapper, new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(String result) {
                    ReferenceGenomeList rgList = AutoBeanCodex.decode(factory, ReferenceGenomeList.class, result).as();
                    refGenList.clear();
                    refGenList.addAll(rgList.getReferenceGenomes());

                    while (!refGenQueue.isEmpty()) {
                        refGenQueue.remove().onSuccess(refGenList);
                    }
                }
            });

        }
        refGenQueue.add(callback);
    }

}