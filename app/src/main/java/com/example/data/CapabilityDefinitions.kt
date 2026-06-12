package com.example.data

sealed class CompetenceCategory(val title: String) {
    object Plan : CompetenceCategory("Plan")
    object Do : CompetenceCategory("Do")
    object Review : CompetenceCategory("Review")
}

data class CapabilityCriterion(
    val id: String,
    val category: CompetenceCategory,
    val title: String,
    val level1Desc: String,
    val level2Desc: String,
    val level3Desc: String
)

object CapabilityDefinitions {
    val criteria = listOf(
        // Plan Section
        CapabilityCriterion(
            id = "MARKET_KNOWLEDGE",
            category = CompetenceCategory.Plan,
            title = "Know my market & customer",
            level1Desc = "Shows a base level of total market knowledge, lacks depth & attention to detail",
            level2Desc = "Demonstrates broad headline knowledge, needs more work to move to 'progressive'",
            level3Desc = "Demonstrates deep & total market awareness across trends, segments, classification & stakeholders."
        ),
        CapabilityCriterion(
            id = "OPPORTUNITIES_IDENTIFIED",
            category = CompetenceCategory.Plan,
            title = "Opportunities identified",
            level1Desc = "Opportunities are linked to current priorities only.",
            level2Desc = "Good consideration for broad set of opportunities, needs work to apply high level prioritisation.",
            level3Desc = "Clearly identified opps on Dist & Activation, with weighted priorities applied, eg, MPA gaps."
        ),
        CapabilityCriterion(
            id = "OBJECTIVES_SET",
            category = CompetenceCategory.Plan,
            title = "Objectives set",
            level1Desc = "Objectives by account set, but not comprehensive or all SMART.",
            level2Desc = "Consistent level of Objectives set with fair level of link to opportunities, refinement required.",
            level3Desc = "SMART objectives linked to priority opportunities and cascaded to accounts accordingly."
        ),
        CapabilityCriterion(
            id = "PREPARED_TO_ACHIEVE",
            category = CompetenceCategory.Plan,
            title = "Prepared to achieve objectives",
            level1Desc = "Some evidence of preparation, gaps on sales tools & pre call articulation on selling std prep.",
            level2Desc = "Good evidence of being prepared, some areas overlooked to be at next level.",
            level3Desc = "Strong evidence of being prepared to achieve obj, sales tools & presentation."
        ),

        // Do Section
        CapabilityCriterion(
            id = "JP_AND_DRIVERS",
            category = CompetenceCategory.Do,
            title = "JP and sales driver checks",
            level1Desc = "Follows JP and shows some evidence of checks on entry, decision makers not always present",
            level2Desc = "Good JP discipline and in call sales driver checks on entry, not always used in presentation as required.",
            level3Desc = "Diligently works to set JP and follows in call sales driver check routines on entry, decision makers present"
        ),
        CapabilityCriterion(
            id = "BRILLIANT_PRESENTATION",
            category = CompetenceCategory.Do,
            title = "Brilliant presentation",
            level1Desc = "Customer interaction was at acceptable standard, but requires better focus on selling standards",
            level2Desc = "Broad adoption of selling standards and profit calc as required, some key 'Stds' overlooked",
            level3Desc = "High standard of 'selling standards' on display at all levels.. Core skills, P.sell, Profit calc."
        ),
        CapabilityCriterion(
            id = "NEXT_STEPS_ADMIN",
            category = CompetenceCategory.Do,
            title = "Next steps & admin",
            level1Desc = "Discusses next steps, but needs more rigour. Admin & SFA usage to an acceptable level.",
            level2Desc = "Applies good focus on next steps & SFA usage, improved Selling skills will present more opportunities",
            level3Desc = "Customer alignment on key next steps, with admin follow up as required & strong SFA usage."
        ),
        CapabilityCriterion(
            id = "ACTIVATION_FOCUS",
            category = CompetenceCategory.Do,
            title = "Activation focus",
            level1Desc = "Performs routine activation where relevant, but low level of seeking quick wins opportunities.",
            level2Desc = "Good activation focus, areas to improve such as full staff alignment.",
            level3Desc = "Strong execution / activation focus, agreed & opportunistic, customers are happy to support."
        ),

        // Review Section
        CapabilityCriterion(
            id = "COMPLIANCE",
            category = CompetenceCategory.Review,
            title = "Compliance",
            level1Desc = "A broad review in this area.",
            level2Desc = "Good attention to detail.",
            level3Desc = "Detailed checks against all agreed areas, contractual & general, gaps identified are acted on."
        ),
        CapabilityCriterion(
            id = "SALES_PERFORMANCE_OUTCOMES",
            category = CompetenceCategory.Review,
            title = "Sales performance outcomes",
            level1Desc = "Uses only anecdotal evidence vs hard data on sales review, no identified actions / decisions.",
            level2Desc = "Good use of available data, needs to be more curious with customers to extract better data.",
            level3Desc = "Uses data to analyse sales performance and makes informed decisions, curious and good Q&L."
        ),
        CapabilityCriterion(
            id = "PERSONAL_REVIEW",
            category = CompetenceCategory.Review,
            title = "Personal review",
            level1Desc = "Ability to share headline feedback on performance, needs depth and link to 'standards'",
            level2Desc = "Good self awareness and eager to develop.",
            level3Desc = "Strong self awareness, ability to recognise strengths and development areas with link to relevant Standards."
        )
    )

    fun getCategoryList(category: CompetenceCategory): List<CapabilityCriterion> {
        return criteria.filter { it.category == category }
    }
}
