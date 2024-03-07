public class Config {
    public static final int NUM_THREADS = 8;

    public static final int eWidth = 33;
    public static final int eHeight = 19;
    public static final int halfEWidth = eWidth>>1;
    public static final int halfEHeight = eHeight>>1;

    public static final int kirbyWidth = 3;
    public static final int kirbyHeight = 3;

    public static final int halfKirbyWidth = kirbyWidth >>1;
    public static final int halfKirbyHeight = kirbyHeight >>1;

    public static final int particleWidth = 3;
    public static final int particleHeight = 3;


    public static final int halfParticleWidth = particleWidth>>1;
    public static final int halfParticleHeight = particleHeight>>1;

    public static final int eParticleWidth = (int) ((double)GUI.canvasWidth/Config.eWidth * particleWidth);
    public static final int eParticleHeight = eParticleWidth;

    public static final int eKirbyWidth = (int) ((double)GUI.canvasWidth/Config.eWidth * kirbyWidth);
    public static final int eKirbyHeight = eKirbyWidth;

    public static final int halfEKirbyWidth = eKirbyWidth>>1;
    public static final int halfEKirbyHeight = eKirbyHeight>>1;

}
