(ns user
  (:require
   [clojure.java.browse :refer [browse-url]]
   [clojure.spec.alpha :as s]
   [clojure.walk :as walk]
   [com.stuartsierra.component :as component]
   com.walmartlabs.lacinia.expound
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.pedestal :as lp]
   [expound.alpha :as expound]
   [honeysql-postgres.format :refer :all]
   [honeysql-postgres.helpers :as psqlh]
   [honeysql.core :as sql]
   [honeysql.helpers :as h]
   [io.pedestal.http :as http]
   [next.jdbc :as jdbc]
   [next.jdbc.specs :as specs]
   [wfs.db.query :as db]
   [wfs.schema :as schema]
   [wfs.system :as system])
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

(alter-var-root #'s/*explain-out* (constantly expound/printer))
