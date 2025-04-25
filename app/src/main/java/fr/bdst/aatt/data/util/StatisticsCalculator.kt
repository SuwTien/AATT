package fr.bdst.aatt.data.util

import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import java.util.*
import kotlin.math.max

/**
 * Classe utilitaire pour les calculs statistiques sur les activités
 */
class StatisticsCalculator {

    /**
     * Représente les statistiques calculées sur une période
     */
    data class ActivityStats(
        val workDuration: Long = 0L,              // Durée totale de travail (VS + DOMICILE)
        val routeDuration: Long = 0L,             // Durée brute de route
        val routeDurationAdjusted: Long = 0L,     // Durée de route après déduction de 1h30 par jour
        val pauseDuration: Long = 0L,             // Durée totale des pauses
        val workDays: Int = 0,                    // Nombre de jours travaillés
        val vsActivityCount: Int = 0,             // Nombre d'activités VS
        val routeActivityCount: Int = 0,          // Nombre d'activités ROUTE
        val domicileActivityCount: Int = 0,       // Nombre d'activités DOMICILE
        val pauseActivityCount: Int = 0           // Nombre d'activités PAUSE
    )

    companion object {
        // Constante pour la déduction quotidienne de route (1h30 = 90 minutes = 5400000 ms)
        private const val ROUTE_DEDUCTION_MS = 5400000L

        /**
         * Calcule les statistiques pour une liste d'activités
         * @param activities Liste des activités à analyser
         * @param calculateByDay Si true, applique la déduction de route par jour. Sinon, une seule déduction.
         */
        fun calculateStats(activities: List<Activity>, calculateByDay: Boolean = true): ActivityStats {
            if (activities.isEmpty()) {
                return ActivityStats()
            }

            var totalWorkDuration = 0L
            var totalRouteDuration = 0L
            var totalPauseDuration = 0L
            
            val vsCount = activities.count { it.type == ActivityType.VS }
            val routeCount = activities.count { it.type == ActivityType.ROUTE }
            val domicileCount = activities.count { it.type == ActivityType.DOMICILE }
            val pauseCount = activities.count { it.type == ActivityType.PAUSE }

            // Si on calcule par jour, on regroupe les activités par jour
            if (calculateByDay) {
                val activitiesByDay = groupActivitiesByDay(activities)
                var workDays = 0
                var routeDeductionTotal = 0L

                for ((_, dayActivities) in activitiesByDay) {
                    var hasWorkThisDay = false
                    var hasRouteThisDay = false
                    var dayWorkDuration = 0L
                    var dayRouteDuration = 0L
                    var dayPauseDuration = 0L

                    // Calcule les durées pour chaque type d'activité de la journée
                    for (activity in dayActivities) {
                        val duration = calculateActivityDuration(activity)
                        
                        when (activity.type) {
                            ActivityType.VS, ActivityType.DOMICILE -> {
                                dayWorkDuration += duration
                                hasWorkThisDay = true
                            }
                            ActivityType.ROUTE -> {
                                dayRouteDuration += duration
                                hasRouteThisDay = true
                            }
                            ActivityType.PAUSE -> {
                                dayPauseDuration += duration
                            }
                        }
                    }
                    
                    // Si il y a eu du travail ou de la route ce jour, c'est un jour travaillé
                    if (hasWorkThisDay || hasRouteThisDay) {
                        workDays++
                    }
                    
                    // Applique la déduction de route si nécessaire
                    if (hasRouteThisDay) {
                        routeDeductionTotal += ROUTE_DEDUCTION_MS
                    }
                    
                    totalWorkDuration += dayWorkDuration
                    totalRouteDuration += dayRouteDuration
                    totalPauseDuration += dayPauseDuration
                }
                
                // Calcule la durée de route ajustée (ne peut pas être négative)
                val routeDurationAdjusted = max(0, totalRouteDuration - routeDeductionTotal)
                
                return ActivityStats(
                    workDuration = totalWorkDuration,
                    routeDuration = totalRouteDuration,
                    routeDurationAdjusted = routeDurationAdjusted,
                    pauseDuration = totalPauseDuration,
                    workDays = workDays,
                    vsActivityCount = vsCount,
                    routeActivityCount = routeCount,
                    domicileActivityCount = domicileCount,
                    pauseActivityCount = pauseCount
                )
            } else {
                // Calcule simplement la durée totale pour chaque type d'activité
                for (activity in activities) {
                    val duration = calculateActivityDuration(activity)
                    
                    when (activity.type) {
                        ActivityType.VS, ActivityType.DOMICILE -> totalWorkDuration += duration
                        ActivityType.ROUTE -> totalRouteDuration += duration
                        ActivityType.PAUSE -> totalPauseDuration += duration
                    }
                }
                
                // Pour un calcul sans groupement par jour, on déduit juste une fois 1h30
                val routeDurationAdjusted = if (totalRouteDuration > 0) {
                    max(0, totalRouteDuration - ROUTE_DEDUCTION_MS)
                } else {
                    0L
                }
                
                val workDays = if (totalWorkDuration > 0 || totalRouteDuration > 0) 1 else 0
                
                return ActivityStats(
                    workDuration = totalWorkDuration,
                    routeDuration = totalRouteDuration,
                    routeDurationAdjusted = routeDurationAdjusted,
                    pauseDuration = totalPauseDuration,
                    workDays = workDays,
                    vsActivityCount = vsCount,
                    routeActivityCount = routeCount,
                    domicileActivityCount = domicileCount,
                    pauseActivityCount = pauseCount
                )
            }
        }

        /**
         * Calcule la durée d'une activité en millisecondes
         * Pour les activités en cours, utilise le temps actuel comme fin
         */
        fun calculateActivityDuration(activity: Activity): Long {
            val startTime = activity.startTime
            val endTime = activity.endTime ?: System.currentTimeMillis()
            return endTime - startTime
        }

        /**
         * Regroupe les activités par jour calendaire
         * @return Map avec les dates (String au format yyyy-MM-dd) comme clés
         */
        private fun groupActivitiesByDay(activities: List<Activity>): Map<String, List<Activity>> {
            val result = mutableMapOf<String, MutableList<Activity>>()
            val calendar = Calendar.getInstance()
            
            for (activity in activities) {
                calendar.timeInMillis = activity.startTime
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val dateKey = "$year-${month+1}-$day"
                
                if (!result.containsKey(dateKey)) {
                    result[dateKey] = mutableListOf()
                }
                
                result[dateKey]?.add(activity)
            }
            
            return result
        }
        
        /**
         * Formatte une durée en millisecondes au format heures:minutes
         */
        fun formatDuration(durationMs: Long): String {
            val hours = durationMs / (1000 * 60 * 60)
            val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
            return String.format("%02d:%02d", hours, minutes)
        }
    }
}