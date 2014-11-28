(ns donkey.core
  (:gen-class)
  (:use [clojure.java.io :only [file]]
        [clojure-commons.lcase-params :only [wrap-lcase-params]]
        [clojure-commons.query-params :only [wrap-query-params]]
        [clj-jargon.init :only [with-jargon]]
        [compojure.core]
        [donkey.routes.admin]
        [donkey.routes.callbacks]
        [donkey.routes.data]
        [donkey.routes.fileio]
        [donkey.routes.metadata]
        [donkey.routes.misc]
        [donkey.routes.notification]
        [donkey.routes.pref]
        [donkey.routes.session]
        [donkey.routes.tree-viewer]
        [donkey.routes.user-info]
        [donkey.routes.collaborator]
        [donkey.routes.filesystem]
        [donkey.routes.search]
        [donkey.routes.coge]
        [donkey.routes.oauth]
        [donkey.routes.favorites]
        [donkey.routes.tags]
        [donkey.routes.comments]
        [donkey.auth.user-attributes]
        [donkey.util]
        [donkey.util.service]
        [ring.middleware keyword-params multipart-params]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [compojure.route :as route]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty]
            [donkey.util.config :as config]
            [donkey.util.db :as db]
            [donkey.services.fileio.controllers :as fileio]
            [donkey.tasks :as tasks]
            [donkey.util.messaging :as messages]
            [donkey.util.icat :as icat]
            [donkey.util.anon :as anon]
            [clojure.tools.nrepl.server :as nrepl]
            [me.raynes.fs :as fs]
            [common-cli.core :as ccli]))

(defn delayed-handler
  [routes-fn]
  (fn [req]
    (let [handler ((memoize routes-fn))]
      (handler req))))

(defn secured-routes
  []
  (flagged-routes
   (secured-notification-routes)
   (secured-metadata-routes)
   (secured-pref-routes)
   (secured-collaborator-routes)
   (secured-user-info-routes)
   (secured-tree-viewer-routes)
   (secured-data-routes)
   (secured-session-routes)
   (secured-fileio-routes)
   (secured-filesystem-routes)
   (secured-filesystem-metadata-routes)
   (secured-coge-routes)
   (secured-admin-routes)
   (secured-search-routes)
   (secured-oauth-routes)
   (secured-favorites-routes)
   (secured-tag-routes)
   (secured-comment-routes)
   (route/not-found (unrecognized-path-response))))

(defn cas-store-user
  [routes]
  (let [f (if (System/getenv "IPLANT_CAS_FAKE") fake-store-current-user store-current-user)]
    (f routes
       config/cas-server
       config/server-name
       config/pgt-callback-base
       config/pgt-callback-path)))

(def secured-handler
  (-> (delayed-handler secured-routes)
      (cas-store-user)))

(defn donkey-routes
  []
  (flagged-routes
   (unsecured-misc-routes)
   (unsecured-notification-routes)
   (unsecured-metadata-routes)
   (unsecured-tree-viewer-routes)
   (unsecured-fileio-routes)
   (unsecured-callback-routes)

   (context "/secured" [] secured-handler)

   (route/not-found (unrecognized-path-response))))

(defn start-nrepl
  []
  (nrepl/start-server :port 7888))

(defn- iplant-conf-dir-file
  [filename]
  (when-let [conf-dir (System/getenv "IPLANT_CONF_DIR")]
    (let [f (file conf-dir filename)]
      (when (.isFile f) (.getPath f)))))

(defn- cwd-file
  [filename]
  (let [f (file filename)]
    (when (.isFile f) (.getPath f))))

(defn- classpath-file
  [filename]
  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (.findResource filename)
      (.toURI)
      (file)))

(defn- no-configuration-found
  [filename]
  (throw (RuntimeException. (str "configuration file " filename " not found"))))

(defn- find-configuration-file
  []
  (let [conf-file "donkey.properties"]
    (or (iplant-conf-dir-file conf-file)
        (cwd-file conf-file)
        (classpath-file conf-file)
        (no-configuration-found conf-file))))

(defn load-configuration-from-file
  "Loads the configuration properties from a file."
  ([]
     (load-configuration-from-file (find-configuration-file)))
  ([path]
     (config/load-config-from-file path)
     (db/define-database)))

(defn lein-ring-init
  []
  (load-configuration-from-file)
  (messages/messaging-initialization)
  (icat/configure-icat)
  (start-nrepl)
  (tasks/schedule-tasks))

(defn repl-init
  []
  (load-configuration-from-file)
  (icat/configure-icat))

(defn site-handler
  [routes-fn]
  (-> (delayed-handler routes-fn)
      (wrap-multipart-params {:store fileio/store-irods})
      trap-handler
      req-logger
      wrap-keyword-params
      wrap-lcase-params
      wrap-query-params))

(def app
  (site-handler donkey-routes))

(defn cli-options
  []
  [["-c" "--config PATH" "Path to the config file"
    :default "/etc/iplant/de/donkey.properties"]
   ["-v" "--version" "Print out the version number."]
   ["-h" "--help"]])

(def svc-info
  {:desc "DE service for business logic"
   :app-name "donkey"
   :group-id "org.iplantc"
   :art-id "donkey"})

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (ccli/handle-args svc-info args cli-options)]
    (when-not (fs/exists? (:config options))
      (ccli/exit 1 (str "The config file does not exist.")))
    (when-not (fs/readable? (:config options))
      (ccli/exit 1 "The config file is not readable."))
    (config/load-config-from-file (:config options))
    (db/define-database)
    (messages/messaging-initialization)
    (icat/configure-icat)
    (anon/create-anon-user)
    (tasks/schedule-tasks)
    (jetty/run-jetty app {:port (config/listen-port)})))