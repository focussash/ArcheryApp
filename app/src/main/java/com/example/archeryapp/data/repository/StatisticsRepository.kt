package com.example.archeryapp.data.repository

import com.example.archeryapp.data.local.dao.ArrowScoreDao
import com.example.archeryapp.data.local.dao.EndDao
import com.example.archeryapp.data.local.dao.SessionDao
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class OverallStatistics(
    val totalSessions: Int,
    val totalEnds: Int,
    val totalArrows: Int,
    val totalScore: Int,
    val totalXCount: Int,
    val total10Count: Int,
    val averageScorePerArrow: Float,
    val averageScorePerEnd: Float,
    val averageArrowsPerSession: Float
)

data class SessionTrendData(
    val sessionId: Long,
    val timestamp: Long,
    val date: LocalDate,
    val averageScore: Float,
    val totalScore: Int,
    val arrowCount: Int,
    val xCount: Int
)

class StatisticsRepository(
    private val sessionDao: SessionDao,
    private val endDao: EndDao,
    private val arrowScoreDao: ArrowScoreDao
) {
    suspend fun getOverallStatistics(): OverallStatistics {
        val totalSessions = sessionDao.getTotalSessionCount()
        val totalEnds = endDao.getTotalEndCount()
        val totalArrows = arrowScoreDao.getTotalArrowCount()
        val totalScore = arrowScoreDao.getTotalScore() ?: 0
        val totalXCount = arrowScoreDao.getTotalXCount()
        val total10Count = arrowScoreDao.getTotal10Count()
        val averageScorePerArrow = arrowScoreDao.getOverallAverageScore() ?: 0f

        val averageScorePerEnd = if (totalEnds > 0) totalScore.toFloat() / totalEnds else 0f
        val averageArrowsPerSession = if (totalSessions > 0) totalArrows.toFloat() / totalSessions else 0f

        return OverallStatistics(
            totalSessions = totalSessions,
            totalEnds = totalEnds,
            totalArrows = totalArrows,
            totalScore = totalScore,
            totalXCount = totalXCount,
            total10Count = total10Count,
            averageScorePerArrow = averageScorePerArrow,
            averageScorePerEnd = averageScorePerEnd,
            averageArrowsPerSession = averageArrowsPerSession
        )
    }

    suspend fun getSessionTrends(daysBack: Int = 30): List<SessionTrendData> {
        val endDate = LocalDate.now().plusDays(1).atStartOfDay()
        val startDate = LocalDate.now().minusDays(daysBack.toLong()).atStartOfDay()

        val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val sessions = sessionDao.getSessionsByDateRangeSync(startMillis, endMillis)

        // Return individual sessions, not grouped by date
        return sessions.map { session ->
            val sessionScore = arrowScoreDao.getTotalScoreForSession(session.id) ?: 0
            val sessionXCount = arrowScoreDao.getXCountForSession(session.id)
            val arrowCount = arrowScoreDao.getArrowCountForSession(session.id)

            val averageScore = if (arrowCount > 0) sessionScore.toFloat() / arrowCount else 0f
            val date = Instant.ofEpochMilli(session.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            SessionTrendData(
                sessionId = session.id,
                timestamp = session.date,
                date = date,
                averageScore = averageScore,
                totalScore = sessionScore,
                arrowCount = arrowCount,
                xCount = sessionXCount
            )
        }.sortedBy { it.timestamp }
    }

    suspend fun getRecentBest(daysBack: Int = 30): Float {
        val trends = getSessionTrends(daysBack)
        return trends.maxOfOrNull { it.averageScore } ?: 0f
    }

    suspend fun getScoreDistribution(): Map<Int, Int> {
        // This would require a more complex query - for now return from calculated data
        val distribution = mutableMapOf<Int, Int>()
        (0..10).forEach { distribution[it] = 0 }

        val sessions = sessionDao.getAllSessionsSync()
        sessions.forEach { session ->
            val ends = endDao.getEndsBySessionIdSync(session.id)
            ends.forEach { end ->
                val arrows = arrowScoreDao.getScoresByEndIdSync(end.id)
                arrows.forEach { arrow ->
                    distribution[arrow.score] = (distribution[arrow.score] ?: 0) + 1
                }
            }
        }

        return distribution
    }
}
