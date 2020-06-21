(ns wfs.subscriptions.dispatch
  (:require
   [clojure.core.async :as a]))

(defn invite
  "Asyncronously send session invites to a list of users.

  Returns a channel that closes after all invites have been sent."
  [{:keys [chan]} from users session]
  (a/onto-chan chan
               (map (partial assoc
                             {:type :invite
                              :payload {:from from :session session}}
                             :deliver-to)
                 users)
               false))
