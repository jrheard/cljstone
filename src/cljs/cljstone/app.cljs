(ns cljstone.app
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board attack make-board summon-minion]]))

(def board (-> (make-board hero/jaina hero/thrall)
               (summon-minion :half-1 minion/chillwind-yeti)
               (summon-minion :half-2 minion/magma-rager)
               (summon-minion :half-1 minion/goldshire-footman)))

(html/draw-board board)

(s/defn perform-attack :- Board
  [character-id-1 :- s/Str
   character-id-2 :- s/Str
   board :- Board]
  (let [character-1 ((board :characters-by-id) character-id-1)
        character-2 ((board :characters-by-id) character-id-2)
        [attacked-character-1 attacked-character-2] (attack character-1 character-2)]
    (-> board
        (assoc-in [:characters-by-id character-id-1] attacked-character-1)
        (assoc-in [:characters-by-id character-id-2] attacked-character-2))))

(defn get-data-transfer [goog-event]
  (-> goog-event
      .getBrowserEvent
      .-dataTransfer))

(defn get-target-dataset [goog-event]
  (-> goog-event
      .-currentTarget
      .-dataset))

(ef/at ".minion" (ev/listen :dragstart
                            (fn [e]
                              (let [minion-id (.-minionId (get-target-dataset e))
                                    data-transfer (get-data-transfer e)]
                                (.setData data-transfer "text/plain" minion-id)))))

(ef/at ".minion"
  (ev/listen :dragover
             (fn [e]
               (.preventDefault e))))

(ef/at ".minion" (ev/listen :drop
                            (fn [e]
                              (let [origin-minion-id (.getData (get-data-transfer e) "text/plain")
                                    destination-minion-id (.-minionId (get-target-dataset e))]
                                (js/console.log origin-minion-id)
                                (js/console.log destination-minion-id)
                                (html/draw-board (perform-attack origin-minion-id destination-minion-id board))
                                (.preventDefault e)))))

