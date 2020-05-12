(ns wfs.db.query
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as jdbc]
   [honeysql.core :as sql]
   [honeysql.helpers :refer [] :as helpers]
   [honeysql-postgres.format :refer :all]
   [honeysql-postgres.helpers :as psqlh]
   [taoensso.timbre :as t]
   [wfs.db.system :refer [sql-format]]))

(defn user-by-id
  [self user-id]
  (->> {:select [[:registered_user/user_id :id]
                 :registered_user/name]
        :from [:registered_user]
        :where [:= :registered_user/user_id user-id]}
       sql-format
       (jdbc/execute-one! (:ds self))))

(defn recipe-by-id
  [self recipe-id]
  (->> {:select [[:recipe/recipe_id :id]
                 :recipe/name
                 :recipe/description
                 :recipe/image]
        :from [:recipe]
        :where [:= :recipe/recipe_id recipe-id]}
       sql-format
       (jdbc/execute-one! (:ds self))))

(defn session-by-id
  [self session-id]
  (->> {:select [[:session/session_id :id]
                 [(sql/call :+ :session/updated_at (sql/inline "INTERVAL '1 hour'")) :expires]]
        :from [:session]
        :where [:= :session/session_id session-id]}
       sql-format
       (jdbc/execute-one! (:ds self))))

(defn session-by-user
  [self user]
  (t/info user)
  (->> {:select [[:session/session_id :id]
                 [(sql/call :+ :session/updated_at (sql/inline "INTERVAL '1 hour'")) :expires]]
        :from [:session]
        :join [:many-session-has-many-user [:= :session/session_id :many-session-has-many-user/session_id_session]]
        :where [:= :many-session-has-many-user/user_id_registered_user (:id user)]}
       sql-format
       (jdbc/execute! (:ds self))
       (#(do (t/info %) %))))
