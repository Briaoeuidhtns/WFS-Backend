(ns wfs.db.mutations
  (:require
   [honeysql.core :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql2]
   [wfs.db.system :refer [sql-format]]))

(defn add-user
  [self user]
  (->> {:insert-into :registered-user
        :values [user]}
       sql-format
       (jdbc/execute-one! (:ds self))
       ::jdbc/update-count
       (= 1)))
