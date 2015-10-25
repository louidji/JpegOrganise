package fr.louidji.tools;

/**
 * Created with IntelliJ IDEA.
 * Bean du resultat de la reorganisation des images.
 * User: louis
 * Date: 09/09/12
 * Time: 10:49
 */
public class Result {
    /**
     * Nombre d'images sources à traiter.
     */
    private int nbImagesToProcess;

    /**
     * Nombre d'images traité avec succès.
     */
    private int nbImagesProcessed;

    /**
     * Initialise l'objet transmettant le resultat du traitement.
     *
     * @param nbImagesToProcess Nombre d'images sources à traiter.
     * @param nbImagesProcessed Nombre d'images traité avec succès.
     */
    @SuppressWarnings("SameParameterValue")
    public Result(int nbImagesToProcess, int nbImagesProcessed) {
        this.nbImagesToProcess = nbImagesToProcess;
        this.nbImagesProcessed = nbImagesProcessed;
    }

    /**
     * Incremente le resultat d'un autre resultat.
     *
     * @param result increment.
     */
    public void add(Result result) {
        nbImagesProcessed = result.getNbImagesProcessed();
        nbImagesToProcess = result.getNbImagesToProcess();
    }

    /**
     * Incremente le resultat d'un autre resultat.
     *
     * @param nbImagesToProcess Nombre d'images sources à traiter.
     * @param nbImagesProcessed Nombre d'images traité avec succès.
     */
    @SuppressWarnings("SameParameterValue")
    public void add(int nbImagesToProcess, int nbImagesProcessed) {
        this.nbImagesToProcess += nbImagesToProcess;
        this.nbImagesProcessed += nbImagesProcessed;
    }

    /**
     * Renvoi le nombre d'images traité avec succès.
     *
     * @return Nombre d'images traité avec succès.
     */
    public int getNbImagesProcessed() {
        return nbImagesProcessed;
    }


    /**
     * Renvoi le nombre d'images traité avec succès.
     *
     * @return Nombre d'images traité avec succès.
     */
    public int getNbImagesToProcess() {
        return nbImagesToProcess;
    }


}
