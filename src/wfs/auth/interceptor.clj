(ns wfs.auth.interceptor
  (:require
   [io.pedestal.interceptor.chain :as chain]
   [io.pedestal.interceptor.helpers :refer [defbefore before]]
   [buddy.auth.backends.token :refer [jws-backend]]
   [buddy.auth.middleware :as middleware]
   [wfs.auth.user :as auth]))

(def auth-backend (jws-backend {:secret auth/pubkey}))

(defbefore
  user-info
  [context]
  (update context :request middleware/authentication-request auth-backend))
