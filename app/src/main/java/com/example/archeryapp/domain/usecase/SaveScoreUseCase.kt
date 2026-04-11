package com.example.archeryapp.domain.usecase

import com.example.archeryapp.data.model.ScoringResult
import com.example.archeryapp.domain.model.Arrow
import com.example.archeryapp.domain.model.End
import com.example.archeryapp.domain.model.Position
import com.example.archeryapp.domain.model.Session
import com.example.archeryapp.domain.model.TargetType
import com.example.archeryapp.domain.repository.ScoreRepository
import com.example.archeryapp.domain.repository.SessionRepository
import java.time.LocalDateTime

class SaveScoreUseCase(
    private val sessionRepository: SessionRepository,
    private val scoreRepository: ScoreRepository
) {
    /**
     * Saves a scoring result as an end in a session.
     * If sessionId is null, creates a new session.
     * Returns pair of (sessionId, endId)
     */
    suspend fun execute(
        scoringResult: ScoringResult,
        sessionId: Long? = null,
        sessionDistance: String? = null,
        sessionBowType: String? = null,
        sessionLocation: String? = null,
        sessionTargetType: TargetType = TargetType.MINI_MC,
        endNotes: String? = null
    ): Result<Pair<Long, Long>> {
        return try {
            // Get or create session. When continuing an existing session we never
            // override its stored targetType — that's locked at creation per spec.
            val actualSessionId = sessionId ?: run {
                val newSession = Session(
                    date = LocalDateTime.now(),
                    distance = sessionDistance,
                    bowType = sessionBowType,
                    location = sessionLocation,
                    targetType = sessionTargetType
                )
                sessionRepository.createSession(newSession)
            }

            // Determine end number
            val endNumber = scoreRepository.getEndCountForSession(actualSessionId) + 1

            // Convert ScoringResult to domain End
            val arrows = scoringResult.scores.mapIndexed { index, arrowScore ->
                // Calculate relative position (0-1) based on target
                val target = scoringResult.target
                val relativeX = (arrowScore.arrow.position.x - target.center.x) / (target.radius * 2) + 0.5f
                val relativeY = (arrowScore.arrow.position.y - target.center.y) / (target.radius * 2) + 0.5f

                Arrow(
                    arrowNumber = index + 1,
                    score = arrowScore.score,
                    isX = arrowScore.isX,
                    position = Position(
                        x = relativeX.coerceIn(0f, 1f),
                        y = relativeY.coerceIn(0f, 1f)
                    )
                )
            }

            val end = End(
                endNumber = endNumber,
                timestamp = LocalDateTime.now(),
                arrows = arrows,
                notes = endNotes
            )

            val endId = scoreRepository.saveEnd(actualSessionId, end)

            Result.success(Pair(actualSessionId, endId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the most recent session, if any.
     */
    suspend fun getMostRecentSession(): Session? {
        return sessionRepository.getMostRecentSession()
    }
}
