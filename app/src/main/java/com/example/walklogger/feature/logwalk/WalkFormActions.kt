package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind

/** The events of the shared walk form; all the UI can reach of [WalkFormEditor]. */
interface WalkFormActions {
    fun setRating(rating: Int)

    /** A null [value] removes the rating for [aspect]. */
    fun setOptionalRating(aspect: OptionalRating, value: Int?)
    fun toggleOptionalRatings()
    fun clearOptionalRatings()

    /** Keeps the description when Yes is picked again; No drops it. A new Yes opens the description rows. */
    fun selectStoolPassed(passed: Boolean)
    fun foldStool()
    fun expandStool()
    fun selectStoolQuantity(value: StoolQuantity)
    fun selectStoolTexture(value: StoolTexture)

    /** A null [value] removes the colour. */
    fun selectStoolColour(value: StoolColour?)
    fun clearStool()

    fun selectEquipment(value: Equipment)

    fun selectRain(value: Rain)
    fun selectTemperature(value: Temperature)
    fun selectWind(value: Wind)
    fun selectSnow(value: Snow)
    fun toggleWeather()
    fun clearWeather()

    fun toggleBehaviourIssue(issue: BehaviourIssue)

    /** Stores the issue (or finds the existing one) and selects it. */
    fun addBehaviourIssue(name: String)
    fun toggleBehaviourIssuesExpanded()
    fun clearBehaviourIssues()

    fun toggleMedicalConcern(concern: MedicalConcern)

    /** Stores the concern (or finds the existing one) and selects it. */
    fun addMedicalConcern(name: String)
    fun toggleMedicalConcernsExpanded()
    fun clearMedicalConcerns()

    fun setNotes(notes: String)
}
