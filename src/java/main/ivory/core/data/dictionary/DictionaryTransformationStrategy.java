package ivory.core.data.dictionary;

import it.unimi.dsi.bits.AbstractBitVector;
import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.TransformationStrategy;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;

public class DictionaryTransformationStrategy implements TransformationStrategy<CharSequence> {
  private static final long serialVersionUID = 1L;
  /** Whether we should guarantee prefix-freeness by adding 0 to the end of each string. */
  private final boolean prefixFree;

  /** Creates an ISO transformation strategy. The strategy will map a string to the lowest eight bits of its natural UTF16 bit sequence.
   * 
   * @param prefixFree if true, the resulting set of binary words will be made prefix free by adding 
   */
  public DictionaryTransformationStrategy( boolean prefixFree ) {
    this.prefixFree = prefixFree;
  }
  
  public static class ISOCharSequenceBitVector extends AbstractBitVector implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient long length;
    private transient long actualEnd;
    byte[] bytes;

    public ISOCharSequenceBitVector( final CharSequence s, final boolean prefixFree) {
      try {
        bytes = s.toString().getBytes("UTF16");
      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      actualEnd = bytes.length * Byte.SIZE;
      //actualEnd = s.length() * Byte.SIZE;
      length = actualEnd + ( prefixFree ? Byte.SIZE * 2: 0 );
      //System.out.println(toString() + " " + s);
    }
    
    public boolean getBoolean( long index ) {
      if ( index > length ) throw new IndexOutOfBoundsException();
      if ( index >= actualEnd ) return false;
      final int byteIndex = (int)( index / Byte.SIZE );
      //return ( (bytes[byteIndex] & 0x80) >>> index % Byte.SIZE ) != 0; 

      return ( bytes[byteIndex] & 0x80 >>> index % Byte.SIZE ) != 0; 
    }
    
    public long length() {
      return length;
    }
  }

  public BitVector toBitVector( final CharSequence s ) {
    return new ISOCharSequenceBitVector( s, prefixFree );
  }

  @Override
  public long numBits() { return 0; }

  @Override
  public TransformationStrategy<CharSequence> copy() {
    return new DictionaryTransformationStrategy(prefixFree);
  }

  public static class Comparator implements java.util.Comparator<String> {
    private final TransformationStrategy<CharSequence> strategy =
      new DictionaryTransformationStrategy(true);

    @Override
    public int compare(String s1, String s2) {
      return strategy.toBitVector(s1).compareTo(strategy.toBitVector(s2));
    }
  }

  public static class WritableComparator extends org.apache.hadoop.io.WritableComparator {
    private final TransformationStrategy<CharSequence> strategy =
        new DictionaryTransformationStrategy(true);

    public WritableComparator() {
      super(Text.class);
    }

    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
      int n1 = WritableUtils.decodeVIntSize(b1[s1]);
      int n2 = WritableUtils.decodeVIntSize(b2[s2]);

      String t1=null, t2=null;
      try {
        t1 = Text.decode(b1, s1+n1, l1-n1);
        t2 = Text.decode(b2, s2+n2, l2-n2);
      } catch (CharacterCodingException e) {
        throw new RuntimeException(e);
      }

      return strategy.toBitVector(t1).compareTo(strategy.toBitVector(t2));
    }
  }
}
