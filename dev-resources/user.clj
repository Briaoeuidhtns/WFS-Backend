(ns user
  (:require
   [wfs.schema :as s]
   [wfs.system :as system]
   [wfs.db.query :as db]
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [io.pedestal.http :as http]
   [clojure.java.browse :refer [browse-url]]
   [clojure.walk :as walk]
   [com.stuartsierra.component :as component]
   [next.jdbc :as jdbc]
   [next.jdbc.specs :as specs]
   [honeysql.core :as sql]
   [honeysql.helpers :as h]
   [honeysql-postgres.format :refer :all]
   [honeysql-postgres.helpers :as psqlh])
  (:import (clojure.lang IPersistentMap)))

(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk
   (fn [node]
     (cond
       (instance? IPersistentMap node)
       (into {} node)

       (seq? node)
       (vec node)

       :else
       node))
   m))

(defonce system (system/new-system))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))

(defn start!
  []
  (alter-var-root #'system component/start-system)
  (browse-url "http://localhost:8888/")
  :started)

(defn stop!
  []
  (alter-var-root #'system component/stop-system)
  :stopped)

(specs/instrument)
