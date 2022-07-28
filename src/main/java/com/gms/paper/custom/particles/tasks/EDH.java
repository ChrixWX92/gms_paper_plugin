package com.gms.paper.custom.particles.tasks;

import java.util.ArrayList;
import java.util.List;

public class EDH implements Runnable
{
    private List<Runnable> list = new ArrayList<Runnable>();

    public void queue(Runnable task)
    {
        list.add(task);
    }

    public void run()
    {
        while(list.size() > 0)
        {
            Runnable task = list.get(0);

            list.remove(0);
            task.run();
        }
    }
}
