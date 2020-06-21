(ns wfs.auth.user
  (:require
   [buddy.auth.backends :as backends]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [buddy.core.keys :as keys]
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [camel-snake-kebab.extras :refer [transform-keys]]
   [com.gfredericks.schpec :as sch]
   [clojure.spec.alpha :as s]
   [slingshot.slingshot :refer [throw+]]
   [taoensso.timbre :refer []]
   [tick.alpha.api :as t]
   [wfs.util :refer [edn-resource]]
   [wfs.db.query :as q]))

(def ^:private jwk (edn-resource "jwk.edn"))
(def ^:private privkey (keys/jwk->private-key jwk))
(def pubkey (keys/jwk->public-key jwk))

(s/def :wfs/username (s/and string? #(< 1 (count %) 128)))
(s/def :wfs/username (s/and string? #(< 1 (count %) 128)))
(s/def :wfs/password (s/and string? #(<= 8 (count %) 128)))
(s/def ::new-user
  (sch/excl-keys :req-un [:wfs/name :wfs/username :wfs/password]))

(defn new
  [user]
  (let [u (s/conform ::new-user user)]
    ()
    (if (= u ::s/invalid)
      (throw+ {:type ::invalid-user :spec-data (s/explain-data ::new-user user)}
              "Invalid user config")
      (update u :password hashers/encrypt))))

(defn claim
  [db attempt]
  (let [good-pw-hash (->> attempt
                          :username
                          (q/user-creds db)
                          :password)
        attempt-pw (:password attempt)]
    (cond (not good-pw-hash) (throw+ {:type ::does-not-exist}
                                     "Can't find the requested user")
          (hashers/check attempt-pw good-pw-hash) (dissoc attempt :password)
          :default (throw+ {:type ::invalid-password}
                           "Password does not match hash"))))

(defn token
  [db creds]
  (let [claim (claim db creds)
        exp (t/+ (t/now) (t/new-duration 1 :hours))]
    (jwt/sign claim privkey {:alg :eddsa :exp exp})))

(defn unsign [msg] (jwt/unsign msg pubkey {:alg :eddsa}))
