(ns wfs.db.query
  (:require
   [com.stuartsierra.component :as component]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as result-set]
   [honeysql.core :as sql]
   [honeysql.helpers :refer [] :as helpers]
   [honeysql-postgres.format :refer :all]
   [honeysql-postgres.helpers :as psqlh]
   [taoensso.timbre :as t]
   [wfs.base64 :refer [b64->str str->b64]]
   [wfs.db.system :refer [sql-format]]))

(def ^:private recipe-base {:select [[:recipe/recipe-id :id]
                                     :recipe/name
                                     :recipe/description
                                     :recipe/image]
                            :from [:recipe]})

(def ^:private session-base {:select [[:session/session-id :id]
                                      [(sql/call
                                        :+ :session/updated-at (sql/inline "INTERVAL '1 hour'"))
                                       :expires]]
                             :from [:session]})

(def ^:private user-base {:select [[:registered-user/user-id :id]
                                   :registered-user/name]
                          :from [:registered-user]})

(def ^:private opts {:builder-fn result-set/as-unqualified-maps})

(defn recipe-by-id
  [self recipe-id]
  (as-> recipe-base $
    (sql/build $ :where [:= :recipe/recipe-id recipe-id])
    (sql-format $)
    (jdbc/execute-one! (:ds self) $ opts)))

(defn session-by-id
  [self session-id]
  (as-> session-base $
    (sql/build $ :where [:= :session/session-id session-id])
    (sql-format $)
    (jdbc/execute-one! (:ds self) $ opts)))

(defn user-by-id
  [self user-id]
  (as-> user-base $
    (sql/build $ :where [:= :registered-user/user-id user-id])
    (sql-format $)
    (jdbc/execute-one! (:ds self) $ opts)))

(defn recipes-by-user
  [self user]
  (as-> recipe-base $
    (sql/build $
               :join [:many-user-has-many-recipe
                      [:= :recipe/recipe-id :many-recipe-has-many-user/recipe-id-recipe]]
               :where [:= :many-user-has-many-recipe/user-id-registered-user (:id user)])
    (sql-format $)
    (jdbc/execute! (:ds self) $ opts)))

(defn sessions-by-user
  [self user]
  (as-> session-base $
    (sql/build $
               :join [:many-session-has-many-user
                      [:= :session/session-id :many-session-has-many-user/session-id-session]]
               :where [:= :many-session-has-many-user/user-id-registered-user (:id user)])
    (sql-format $)
    (jdbc/execute! (:ds self) $ opts)))

(defn users-by-session
  [self session]
  (as-> user-base $
    (sql/build $
               :join [:many-session-has-many-user
                      [:= :registered-user/user-id :many-session-has-many-user/user-id-registered-user]]
               :where [:= :many-session-has-many-user/session-id-session (:id session)])
    (sql-format $)
    (jdbc/execute! (:ds self) $ opts)))


(defn recipes-connection-by-session
  [self session]
  (t/warn "recipes_connection not implemented")
  {:page_info {:has_previous_page false
               :has_next_page false
               :start_cursor (-> 0 str str->b64)
               :end_cursor (-> 1 str str->b64)}
   :edges []})
