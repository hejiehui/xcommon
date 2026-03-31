package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.parts.AbstractConnectionEditPart;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;

import java.util.List;

public abstract class AbstractRouter implements ConnectionRouter {
    public static final int DEFAULT_GAP = 50;

    public abstract void routeDifferentNode(Connection conn);

    public abstract void routeSameNode(Connection conn);

    @Override
    public void route(Connection conn) {
        if (conn.isConnectToSameNode()) {
            routeSameNode(conn);
        }else {
            routeDifferentNode(conn);
        }
    }

    public int indexOfSameStyle(Connection conn) {
        List<AbstractConnectionEditPart> outputs =  ((AbstractGraphicalEditPart)conn.getConnectionPart().getParent()).getSourceConnections();
        AbstractConnectionEditPart curConn = conn.getConnectionPart();

        int i = 0;
        for (AbstractConnectionEditPart  output: outputs) {
            if (isSameStyle(output, curConn) && output.getSource() == output.getTarget())
                i++;

            if (output == curConn)
                break;
        }
        return i;
    }

    public boolean isSameStyle(AbstractConnectionEditPart connPart1, AbstractConnectionEditPart connPart2) {
        ConnectionRouter r1 = ((Connection)connPart1.getFigure()).getRouter();
        ConnectionRouter r2 = ((Connection)connPart2.getFigure()).getRouter();
        return r1 != null && r2 != null && r1.equals(r2);
    }
}
