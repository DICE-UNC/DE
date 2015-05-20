(ns metadactyl.routes.api
  (:use [clojure-commons.query-params :only [wrap-query-params]]
        [compojure.api.sweet]
        [compojure.api.legacy]
        [metadactyl.routes.domain.analysis]
        [metadactyl.routes.domain.analysis.listing]
        [metadactyl.routes.domain.app]
        [metadactyl.routes.domain.app.category]
        [metadactyl.routes.domain.app.element]
        [metadactyl.routes.domain.app.rating]
        [metadactyl.routes.domain.callback]
        [metadactyl.routes.domain.oauth]
        [metadactyl.routes.domain.pipeline]
        [metadactyl.routes.domain.reference-genome]
        [metadactyl.routes.domain.tool]
        [metadactyl.routes.params]
        [metadactyl.schema.containers]
        [metadactyl.user :only [store-current-user]]
        [ring.middleware keyword-params nested-params]
        [ring.swagger.json-schema :only [json-type]]
        [ring.swagger.schema :only [describe]]
        [ring.util.response :only [redirect]])
  (:require [metadactyl.routes.admin :as admin-routes]
            [metadactyl.routes.analyses :as analysis-routes]
            [metadactyl.routes.apps :as app-routes]
            [metadactyl.routes.apps.categories :as app-category-routes]
            [metadactyl.routes.apps.elements :as app-element-routes]
            [metadactyl.routes.apps.pipelines :as pipeline-routes]
            [metadactyl.routes.callbacks :as callback-routes]
            [metadactyl.routes.oauth :as oauth-routes]
            [metadactyl.routes.reference-genomes :as reference-genome-routes]
            [metadactyl.routes.tools :as tool-routes]
            [service-logging.thread-context :as tc]))

(defmethod json-type schema.core.AnythingSchema [_] {:type "any"})

(def context-map (ref {}))

(defn set-context-map!
  "Sets the map that will be used to create the ThreadContext by wrap-context-map."
  [cm]
  (dosync (ref-set context-map cm)))

(defn wrap-context-map
  "Sets the ThreadContext for each request."
  [handler]
  (fn [request]
    (tc/set-context! @context-map)
    (let [resp (handler request)]
      (tc/clear-context!)
      resp)))

(defapi app
  (swagger-ui "/api")
  (swagger-docs "/api/api-docs"
    :title "Discovery Environment Apps API"
    :description "Documentation for the Discovery Environment Apps REST API"
    :apiVersion "2.0.0")
  (GET "/" [] (redirect "/api"))
  (GET "/favicon.ico" [] {:status 404})
  (middlewares
    [wrap-keyword-params
     wrap-query-params
     wrap-context-map]
    (swaggered "callbacks"
      :description "General callback functions."
      (context "/callbacks" [] callback-routes/callbacks)))
  (middlewares
    [wrap-keyword-params
     wrap-query-params
     tc/add-user-to-context
     wrap-context-map
     store-current-user]
    (swaggered "app-categories"
      :description "App Category endpoints."
      (context "/apps/categories" [] app-category-routes/app-categories))
    (swaggered "app-element-types"
      :description "App Element endpoints."
      (context "/apps/elements" [] app-element-routes/app-elements))
    (swaggered "apps"
      :description "App endpoints."
      (context "/apps" [] app-routes/apps))
    (swaggered "pipelines"
      :description "Pipeline endpoints."
      (context "/apps/pipelines" [] pipeline-routes/pipelines))
    (swaggered "analyses"
      :description "Analysis endpoints."
      (context "/analyses" [] analysis-routes/analyses))
    (swaggered "tools"
      :description "Tool endpoints."
      tool-routes/tools)
    (swaggered "tool-requests"
      :description "Tool Request endpoints."
      (context "/tool-requests" [] tool-routes/tool-requests))
    (swaggered "reference-genomes"
      :description "Reference Genome endpoints."
      (context "/reference-genomes" [] reference-genome-routes/reference-genomes))
    (swaggered "oauth-routes"
      :description "OAuth callback routes."
      (context "/oauth" [] oauth-routes/oauth))
    (swaggered "admin-apps"
      :description "Admin App endpoints."
      (context "/admin/apps" [] admin-routes/admin-apps))
    (swaggered "admin-categories"
      :description "Admin App Category endpoints."
      (context "/admin/apps/categories" [] admin-routes/admin-categories))
    (swaggered "admin-reference-genomes"
      :description "Admin Reference Genome endpoints."
      (context "/admin/reference-genomes" [] admin-routes/reference-genomes))
    (swaggered "admin-tools"
      :description "Admin Tool endpoints."
      (context "/admin/tools" [] tool-routes/admin-tools))
    (swaggered "admin-tool-requests"
      :description "Admin Tool Request endpoints."
      (context "/admin/tool-requests" [] admin-routes/admin-tool-requests))))
