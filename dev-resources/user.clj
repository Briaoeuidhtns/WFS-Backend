(ns user
  (:require
   [clojure.java.browse :refer [browse-url]]
   [clojure.spec.alpha :as s]
   [clojure.walk :as walk]
   [com.stuartsierra.component :as component]
   com.walmartlabs.lacinia.expound
   [com.walmartlabs.lacinia :as lacinia]
   [com.walmartlabs.lacinia.pedestal2 :as lp]
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
   [wfs.system :as system]
   [wfs.test.util :refer [simplify]]
   [clojure.core.async :refer [<! >! <!! >!! go] :as a])
  (:import
   (clojure.lang IPersistentMap)))



(defrecord Started [started]
  component/Lifecycle
    (start [self] (assoc self :started true))
    (stop [self] (assoc self :started nil)))

(defonce system (atom {}))

(defn q
  [query-string]
  (-> system
      :schema-provider
      :schema
      (lacinia/execute query-string nil nil)
      simplify))

(defn start!
  []
  (swap! system component/start-system)
  (browse-url "http://localhost:8888/ide")
  :started)

(defn stop! [] (swap! system component/stop-system) :stopped)

(defn new-system!
  []
  (when (get-in @system [:status :started]) (stop!))
  (reset! system (assoc (system/new-system) :status (map->Started {}))))

(specs/instrument)

(alter-var-root #'s/*explain-out* (constantly expound/printer))
