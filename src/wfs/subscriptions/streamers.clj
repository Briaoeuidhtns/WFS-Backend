(ns wfs.subscriptions.streamers
  (:require
   [clojure.core.async :as a :refer [<!]]
   [wfs.auth.user :as user]
   [taoensso.timbre :as log]
   [slingshot.slingshot :refer [throw+]]
   [wfs.auth.user :as auth]))

(defn invites
  [{:keys [pub]}]
  (fn [{{auth-user :username} ::user/identity} {arg-user :claim} handle]
    (let [user (or (:username (auth/unsign arg-user)) auth-user)]
      (when-not user
        (throw+ {:type :unauthorized
                 :message "Can't subscribe without authentication"}))
      (let [tag {:deliver-to user :type :invite}
            ch (a/chan (a/sliding-buffer 5))] ; Super arbitrary
        (a/sub pub tag ch)
        (a/go-loop []
                   (let [{:keys [payload] :as ob} (<! ch)]
                     (log/debug "Sending" user payload)
                     (handle payload)
                     (recur)))
        (fn [] (a/unsub pub tag ch))))))
