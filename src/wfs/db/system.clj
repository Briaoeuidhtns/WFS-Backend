(ns wfs.db.system
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as jdbc]
   [honeysql.core :as sql]
   [honeysql-postgres.format :refer :all]
   [honeysql-postgres.helpers :as psqlh]
   [taoensso.timbre :as t]))

(defrecord Database [ds]
  component/Lifecycle

  (start [self]
    (assoc self
           :ds (jdbc/get-datasource {:dbtype "pgsql"
                                     :dbname "wfs"
                                     :port 25432
                                     :user "postgres"
                                     :password "wfspassword"})))

  ;; TODO close it?
  ;; Can't tell if it needs to happen but it isn't Closeable
  (stop [self]
    (assoc self :db nil)))

(defn new-db []
  {:db (map->Database {})})

(defn sql-format
  [sql-map]
  (let [q (sql/format sql-map :namespace-as-table? true)]
    (t/info q)
    q))
