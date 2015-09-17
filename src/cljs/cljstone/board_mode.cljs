(ns cljstone.board-mode
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card]]
        [cljstone.character :only [Character]]
        [cljstone.minion :only [Minion]]))

(s/defschema DefaultMode
  {:type :default})

(s/defschema PositioningMode
  {:type :positioning
   :minion Minion
   :continuation s/Any})

(s/defschema TargetingMode
  {:type :targeting
   :targets [Character]
   ; choose-one mode will also have an optional positioning-info
   ; this is because minions can be positioned and can then have targetable battlecries or choose-ones
   (s/optional-key :positioning-info) {:minion Minion
                                       :index s/Int}
   :continuation s/Any})

(s/defschema MulliganMode
  {:type :mulligan
   :cards [Card]
   :continuation s/Any})

(s/defschema BoardMode
  (s/enum
    DefaultMode
    PositioningMode
    TargetingMode
    MulliganMode))
