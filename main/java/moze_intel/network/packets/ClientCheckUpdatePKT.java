package moze_intel.network.packets;

import io.netty.buffer.ByteBuf;
import moze_intel.utils.ThreadCheckUpdate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ClientCheckUpdatePKT implements IMessage, IMessageHandler<ClientCheckUpdatePKT, IMessage> 
{
	@Override
	public IMessage onMessage(ClientCheckUpdatePKT message, MessageContext ctx)
	{
		new ThreadCheckUpdate(false).start();
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}
}
