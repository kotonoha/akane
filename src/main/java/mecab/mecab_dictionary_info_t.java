package mecab;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * DictionaryInfo structure<br>
 * <i>native declaration : line 16</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("mecab") 
public class mecab_dictionary_info_t extends StructObject {
	public mecab_dictionary_info_t() {
		super();
	}
	/**
	 * filename of dictionary<br>
	 * On Windows, filename is stored in UTF-8 encoding<br>
	 * C type : const char*
	 */
	@Field(0) 
	public Pointer<Byte > filename() {
		return this.io.getPointerField(this, 0);
	}
	/**
	 * filename of dictionary<br>
	 * On Windows, filename is stored in UTF-8 encoding<br>
	 * C type : const char*
	 */
	@Field(0) 
	public mecab_dictionary_info_t filename(Pointer<Byte > filename) {
		this.io.setPointerField(this, 0, filename);
		return this;
	}
	/**
	 * character set of the dictionary. e.g., "SHIFT-JIS", "UTF-8"<br>
	 * C type : const char*
	 */
	@Field(1) 
	public Pointer<Byte > charset() {
		return this.io.getPointerField(this, 1);
	}
	/**
	 * character set of the dictionary. e.g., "SHIFT-JIS", "UTF-8"<br>
	 * C type : const char*
	 */
	@Field(1) 
	public mecab_dictionary_info_t charset(Pointer<Byte > charset) {
		this.io.setPointerField(this, 1, charset);
		return this;
	}
	/// How many words are registered in this dictionary.
	@Field(2) 
	public int size() {
		return this.io.getIntField(this, 2);
	}
	/// How many words are registered in this dictionary.
	@Field(2) 
	public mecab_dictionary_info_t size(int size) {
		this.io.setIntField(this, 2, size);
		return this;
	}
	/**
	 * dictionary type<br>
	 * this value should be MECAB_USR_DIC, MECAB_SYS_DIC, or MECAB_UNK_DIC.
	 */
	@Field(3) 
	public int type() {
		return this.io.getIntField(this, 3);
	}
	/**
	 * dictionary type<br>
	 * this value should be MECAB_USR_DIC, MECAB_SYS_DIC, or MECAB_UNK_DIC.
	 */
	@Field(3) 
	public mecab_dictionary_info_t type(int type) {
		this.io.setIntField(this, 3, type);
		return this;
	}
	/// left attributes size
	@Field(4) 
	public int lsize() {
		return this.io.getIntField(this, 4);
	}
	/// left attributes size
	@Field(4) 
	public mecab_dictionary_info_t lsize(int lsize) {
		this.io.setIntField(this, 4, lsize);
		return this;
	}
	/// right attributes size
	@Field(5) 
	public int rsize() {
		return this.io.getIntField(this, 5);
	}
	/// right attributes size
	@Field(5) 
	public mecab_dictionary_info_t rsize(int rsize) {
		this.io.setIntField(this, 5, rsize);
		return this;
	}
	/// version of this dictionary
	@Field(6) 
	public short version() {
		return this.io.getShortField(this, 6);
	}
	/// version of this dictionary
	@Field(6) 
	public mecab_dictionary_info_t version(short version) {
		this.io.setShortField(this, 6, version);
		return this;
	}
	/**
	 * pointer to the next dictionary info.<br>
	 * C type : mecab_dictionary_info_t*
	 */
	@Field(7) 
	public Pointer<mecab_dictionary_info_t > next() {
		return this.io.getPointerField(this, 7);
	}
	/**
	 * pointer to the next dictionary info.<br>
	 * C type : mecab_dictionary_info_t*
	 */
	@Field(7) 
	public mecab_dictionary_info_t next(Pointer<mecab_dictionary_info_t > next) {
		this.io.setPointerField(this, 7, next);
		return this;
	}
	public mecab_dictionary_info_t(Pointer pointer) {
		super(pointer);
	}
}