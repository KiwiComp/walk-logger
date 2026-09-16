package com.example.walklogger.feature.logwalk.component

import com.example.walklogger.core.database.DefaultBehaviourIssues
import com.example.walklogger.core.database.DefaultMedicalConcerns
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LabelsTest {

    @Test
    fun `every seeded behaviour issue has a localised label`() {
        DefaultBehaviourIssues.keys.forEach { key -> assertNotNull(key, behaviourIssueLabelRes(key)) }
    }

    @Test
    fun `every seeded medical concern has a localised label`() {
        DefaultMedicalConcerns.keys.forEach { key -> assertNotNull(key, medicalConcernLabelRes(key)) }
    }

    @Test
    fun `user-added items fall back to their name`() {
        assertNull(behaviourIssueLabelRes(null))
        assertNull(behaviourIssueLabelRes("digging"))
        assertNull(medicalConcernLabelRes(null))
        assertNull(medicalConcernLabelRes("coughing"))
    }
}
