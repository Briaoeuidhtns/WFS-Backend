(ns keygen
  (:require
   [clojure.java.io :as io]
   [buddy.core.keys :as keys])
  (:import
   (net.i2p.crypto.eddsa KeyPairGenerator)))

(defn create
  []
  (-> (KeyPairGenerator.)
      .generateKeyPair
      bean))

(defn -main
  [dir]
  (->> (create)
       ((juxt :private :public))
       (apply keys/jwk)
       (spit (io/file dir "jwk.edn"))))
