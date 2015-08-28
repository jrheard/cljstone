(ns cljstone.app
  (:require [reagent.core :as r]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board summon-minion]]))

(def next-character-id (atom 0))

(defn get-next-character-id []
  (let [id-to-return @next-character-id]
    (swap! next-character-id inc)
    id-to-return))

(def jaina (hero/make-hero "Jaina" :mage (get-next-character-id)))
(def thrall (hero/make-hero "Thrall" :shaman (get-next-character-id)))

(def board (r/atom (-> (make-board jaina thrall)
                     (summon-minion :half-1 minion/chillwind-yeti (get-next-character-id))
                     (summon-minion :half-2 minion/magma-rager (get-next-character-id))
                     (summon-minion :half-1 minion/goldshire-footman (get-next-character-id)))))

(html/mount-reagent board)
