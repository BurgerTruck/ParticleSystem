public class Config {
    public static final int NUM_THREADS = 8;

    private static final double scaleX = (double)GUI.canvasWidth/Config.eWidth;
    private static final double scaleY = (double)GUI.canvasHeight/Config.eHeight;
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

    public static final int eParticleWidth = (int) (scaleX * particleWidth);
    public static final int eParticleHeight = eParticleWidth;

    public static final int halfEParticleWidth = eParticleWidth>>1;
    public static final int halfEParticleHeight = eParticleHeight>>1;

    public static  int eKirbyWidth = (int) (scaleX * kirbyWidth);
    public static final int eKirbyHeight ;
    public static final  int halfEKirbyWidth;
    public static final  int halfEKirbyHeight;


    public static final Position[] leftBoundRect;
    public static final Position[] topBoundRect;
    public static final Position[] rightBoundRect;
    public static final Position[] bottomBoundRect;


    public static final int NUM_PARTICLES_PER_PACKET = 4000;
    static{
        if((eKirbyWidth&1)==0) eKirbyWidth++;
        eKirbyHeight = eKirbyWidth;
        halfEKirbyWidth = eKirbyWidth>>1;
        halfEKirbyHeight = eKirbyHeight>>1;

        leftBoundRect = new Position[]{new Position(-halfEWidth-1,GUI.canvasHeight+halfEHeight), new Position(0,  -halfEHeight-1)};
        rightBoundRect = new Position[]{new Position(GUI.canvasWidth-1, GUI.canvasHeight+halfEHeight), new Position(GUI.canvasWidth+halfEWidth, -halfEHeight-1)};

        topBoundRect = new Position[]{new Position(-halfEWidth-1, GUI.canvasHeight+halfEHeight+1), new Position(GUI.canvasWidth+halfEWidth, GUI.canvasHeight-1)};
        bottomBoundRect = new Position[]{new Position(-halfEWidth-1, 0), new Position(GUI.canvasWidth+halfEWidth, -halfEHeight-1)};
    }

}
