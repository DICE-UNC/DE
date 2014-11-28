(defproject org.iplantc/clavin "3.2.7"
  :description "A command-line tool for loading service configurations."
  :url "http://www.iplantcollaborative.org"
  :license {:name "BSD"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :dependencies [[org.antlr/stringtemplate "4.0.2"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.1"]
                 [medley "0.1.5"]
                 [me.raynes/fs "1.4.4"]
                 [org.iplantc/clojure-commons "3.2.7"]]
  :plugins [[org.iplantc/lein-iplant-cmdtar "3.2.7"]
            [org.iplantc/lein-iplant-rpm "3.2.7"]]
  :iplant-rpm {:summary "Clavin"
               :type :command
               :provides "iplant-clavin"}
  :aot [clavin.core]
  :main clavin.core
  :repositories [["sonatype-nexus-snapshots"
                  {:url "https://oss.sonatype.org/content/repositories/snapshots"}]]
  :deploy-repositories [["sonatype-nexus-staging"
                         {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"}]])