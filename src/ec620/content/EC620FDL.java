package ec620.content;

import arc.Core;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.math.geom.Vector;
import arc.struct.Seq;
import arc.util.Log;
import static ec620.content.EC620Classes.*;

public class EC620FDL
{
    public Seq<Node> Nodes=new Seq<>();

    static final int seed = (int)(System.currentTimeMillis()%Integer.MAX_VALUE);
    private Rand pgRand=new Rand(seed);
    private double g=9.8f,k=.01d,l,m=100d;
    private int i=0,width,height;
    class Node
    {
        public DVec Pos;
        public Seq<Node> Links;
        public int ID;
        private DVec force;
        private DVec verocity;
        private double edge;

        public Node(DVec pos)
        {
            Pos=pos;
            Links=new Seq<>();
            l= (double)Core.settings.getInt("ec620.mapSize")/10d;
            edge=l/2d;
            ID=i++;
        }
        public void Reset()
        {
            force=new DVec();
            verocity=new DVec();
        }
        public void AddForce(double s,DVec v)
        {
            force.Add(s,v);
        }
        public void Update(double t)
        {
            Pos.x = Mathf.clamp(force.x * t * t / 2d + t * verocity.x + Pos.x, edge,width-edge);
            Pos.y = Mathf.clamp(force.y * t * t / 2d + t * verocity.y + Pos.y, edge,height-edge);
            verocity.Add(t,force);
            force=new DVec();
            //Log.info(ID+": "+"("+Pos.x+", "+Pos.y+")");
        }
    }
    public EC620FDL(int w,int h)
    {
        width=w;
        height=h;
    }

    public DVec GetPos(int index)
    {
        return Nodes.get(index).Pos;
    }
    public Seq<Integer> GetLinks(int index)
    {
        return Nodes.get(index).Links.map(n->n.ID);
    }
    private void randomLink()
    {
        for(Node node:Nodes)
        {
            Node tNode=Nodes.getFrac(pgRand.nextFloat());
            if(!node.Links.contains(tNode))
            {
                node.Links.add(tNode);
                tNode.Links.add(node);
            }
        }
    }
    public void AddNode(DVec pos)
    {
        Nodes.add(new Node(pos));
    }
    public void Arrange()
    {
        Nodes.forEach(n->n.Reset());
        randomLink();
        for(int i=0;i<Core.settings.getInt("ec620.fdl");i++)
        {
            for(Node n0:Nodes)
            {
                for(Node n1:Nodes)
                {
                    if(n0!=n1)
                    {
                        double r= n0.Pos.DistanceFrom(n1.Pos);
                        DVec v=new DVec(n0.Pos.x-n1.Pos.x,n0.Pos.y-n1.Pos.y);
                        double f;

                        if(r>0) f=(g*m*m)/(r*r*r);
                        else f = Integer.MAX_VALUE;
                        //Log.info("Node Force: "+f);
                        n0.AddForce(f,v);
                    }
                }
                for(Node n1:n0.Links)
                {
                    double r= n0.Pos.DistanceFrom(n1.Pos);

                    if(r==0) r=Double.MIN_NORMAL;
                    DVec uv=new DVec((n1.Pos.x-n0.Pos.x)/r,(n1.Pos.y-n0.Pos.y)/r);
                    double f=k*(r-l);
                    //Log.info("Link Force: "+f);
                    n0.AddForce(f,uv);
                }
            }
            Nodes.forEach(n->n.Update(.01d));
        }
    }
}
