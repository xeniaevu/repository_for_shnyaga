package mousemaze.training;

/** Наблюдатель за ходом обучения (например, полоса прогресса в GUI). */
@FunctionalInterface
public interface TrainingListener {

    void onEpisodeFinished(EpisodeResult result, int totalEpisodes);
}
