(ns cljstone.board
  (:require [schema.core :as s]
            [cljstone.hero :as hero])
  (:use [cljstone.minion :only [Minion MinionSchematic Modifier get-attack make-minion]]))

(def BoardHalf
  {:index s/Int
   :hero hero/Hero
   :minion-ids [s/Str]})

(def Character
  {:id s/Str
   :base-health s/Int
   :base-attack s/Int
   ; TODO - modifiers?
   s/Any s/Any})

(def Board
  {:half-1 BoardHalf
   :half-2 BoardHalf
   ; hm - if we made this next field an atom, we could attach watchers to it
   ; might be an interesting way of powering events
   ; doesn't cover everything though (blessing of wisdom)
   :characters-by-id {s/Str Character}})

(s/defn make-board :- Board
  [hero-1 :- hero/Hero
   hero-2 :- hero/Hero]
  {:half-1 {:index 0 :hero hero-1 :minion-ids []}
   :half-2 {:index 1 :hero hero-2 :minion-ids []}
   :characters-by-id {}})

(s/defn summon-minion :- Board
  [board :- Board
   which-board-half :- (s/enum :half-1 :half-2)
   schematic :- MinionSchematic]
  (let [minion (make-minion schematic)]
    (-> board
        (update-in [:characters-by-id] assoc (:id minion) minion)
        (update-in [which-board-half :minion-ids] conj (:id minion)))))

(s/defn print-character [character :- Character]
  (js/console.log (clj->js (select-keys character [:id :base-health :base-attack]))))

(s/defn create-attack-modifier :- Modifier
  [character :- Character]
  {:type :attack
   :name nil
   :effects {:health (- (get-attack character))}})

(s/defn attack :- Board
  [character-id-1 :- s/Str
   character-id-2 :- s/Str
   board :- Board]
    (let [character-1 ((board :characters-by-id) character-id-1)
          character-2 ((board :characters-by-id) character-id-2)]
      (print-character character-1)
      (print-character character-2)
      (js/console.log (clj->js (create-attack-modifier character-1)))
      ; TODO: create attack modifiers, assoc them onto each minion's :modifiers list, return a board
      )
  )
