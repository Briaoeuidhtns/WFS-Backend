(ns wfs.db.mutations
  (:require
   [honeysql.core :as sql]
   [honeysql-postgres.format :refer :all]
   [next.jdbc :as jdbc]
   [wfs.db.system :refer [sql-format]]))

(defn add-user
  [self user]
  (->> {:insert-into :registered-user
        :values [user]}
       sql-format
       (jdbc/execute-one! (:ds self))
       ::jdbc/update-count
       (= 1)))

(defn start-session
  "Start a new session, adding the current user as the only member"
  [self user]
  (jdbc/with-transaction
    [ds (:ds self)]
    (let [session-id (->> {:insert-into :session
                           :values [{:session-id (sql/inline "DEFAULT")}]
                           :returning [:session/session_id]}
                          sql-format
                          (jdbc/execute-one! ds)
                          :session/session_id)]
      (->> {:insert-into :many-session-has-many-user
            :values [{:session_id_session session-id
                      :username_registered_user (:username user)}]
            :returning [:session_id_session]}
           sql-format
           (jdbc/execute-one! ds)
           :many_session_has_many_user/session_id_session))))

;; TODO use a nested insert, the below works
;; Can't figure out how to translate it though

;; WITH sess AS (INSERT INTO "session" (session_id) VALUES (default) RETURNING
;; session.session_id)
;; INSERT INTO many_session_has_many_user (session_id_session,
;; username_registered_user) SELECT sess.session_id, 'brian' FROM sess
;; RETURNING session_id_session
