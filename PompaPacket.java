
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public abstract class PompaPacket {
	private static List<Class<? extends PompaPacket>> packetLinks;
	private static Map<String, Integer> packetIDLinks;

	private static void registerPacket(Class<? extends PompaPacket> clazz) {
		packetIDLinks.put(clazz.getName(), packetLinks.size());
		packetLinks.add(PompaGameState.class);
	}

	static {
		packetLinks = new ArrayList<Class<? extends PompaPacket>>();
		packetIDLinks = new HashMap<String, Integer>();

		registerPacket(PompaGameState.class);
	}

	public static PompaPacket readPacket(DataInputStream dIN)
			throws IOException {
		int id = dIN.read();
		if (id >= 0 && id < packetLinks.size()) {
			Class<? extends PompaPacket> clazz = packetLinks.get(id);
			try {
				PompaPacket pack = clazz.newInstance();
				pack.readData(dIN);
				return pack;
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.err.println("BAD PACKET ID: " + id);
		return null;
	}

	public abstract void readData(DataInputStream dIN) throws IOException;

	public abstract void writeData(DataOutputStream dOUT) throws IOException;

	public static byte[] createPacket(String server, PompaPacket pack)
			throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dOUT = new DataOutputStream(bytes);
		Integer id = packetIDLinks.get(pack.getClass().getName());
		if (id == null) {
			throw new IOException("BAD PACKET");
		}
		dOUT.write(id.intValue());
		pack.writeData(dOUT);
		dOUT.close();
		return bytes.toByteArray();
	}

	protected static void writeEnum(DataOutputStream dOUT, Enum e)
			throws IOException {
		dOUT.writeInt(e.ordinal());
	}

	protected static <T extends Enum> T readEnum(DataInputStream dIN,
			Class<T> clazz) throws IOException {
		int ordinal = dIN.readInt();
		try {
			@SuppressWarnings("unchecked")
			T[] values = (T[]) clazz.getMethod("values").invoke(null);
			return values[ordinal];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}