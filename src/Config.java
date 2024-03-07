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

    public static  int eKirbyWidth = (int) ((double)GUI.canvasWidth/Config.eWidth * kirbyWidth);
    public static final int eKirbyHeight ;
    public static final  int halfEKirbyWidth;
    public static final  int halfEKirbyHeight;

    static{
        if((eKirbyWidth&1)==0) eKirbyWidth++;
        eKirbyHeight = eKirbyWidth;
        halfEKirbyWidth = eKirbyWidth>>1;
        halfEKirbyHeight = eKirbyHeight>>1;
    }

}
