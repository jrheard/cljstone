(ns cljstone.armory
  (:require [schema.core :as s]))

(s/defschema WeaponModifier
  {})

(s/defschema WeaponSchematic
  {:name s/Str
   :attack s/Int
   :durability s/Int
   :mana-cost s/Int
   :modifiers [WeaponModifier]
   :class (s/enum :warrior :rogue :paladin)})

(def all-weapons
  {:arcanite-reaper {:name "Arcanite Reaper", :attack 5, :durability 2, :mana-cost 5, :class :warrior}
   :assassins-blade {:name "Assassin's Blade", :attack 3, :durability 4, :mana-cost 5, :class :rogue}
   :fiery-win-axe {:name "Fiery War Axe", :attack 3, :durability 2, :mana-cost 2, :class :warrior}
   :lights-justice {:name "Light's Justice", :attack 1, :durability 4, :mana-cost 1, :class :paladin}
   :wicked-knife {:name "Wicked Knife", :attack 1, :durability 2, :mana-cost 2, :class :rogue}
   ; XXXX TODO :truesilver-champion {:name "Truesilver Champion", :attack 4, :durability 2, :mana-cost 4, :class :paladin}
   })
