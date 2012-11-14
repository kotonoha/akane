/*
 * Copyright 2012 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mecab;
import org.bridj.BridJ;
import org.bridj.Pointer;
import org.bridj.TypedPointer;
import org.bridj.ann.Library;
import org.bridj.ann.Name;
import org.bridj.ann.Ptr;
import org.bridj.ann.Runtime;
import org.bridj.cpp.CPPRuntime;
import org.bridj.util.DefaultParameterizedType;
/**
 * Wrapper for library <b>mecab</b><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("mecab") 
@Runtime(CPPRuntime.class) 
public class MecabLibrary {
	static {
		BridJ.register();
	}
	/// Normal node defined in the dictionary.
	public static final int MECAB_NOR_NODE = (int)0;
	/// Unknown node not defined in the dictionary.
	public static final int MECAB_UNK_NODE = (int)1;
	/// Virtual node representing a beginning of the sentence.
	public static final int MECAB_BOS_NODE = (int)2;
	/// Virtual node representing a end of the sentence.
	public static final int MECAB_EOS_NODE = (int)3;
	/// Virtual node representing a end of the N-best enumeration.
	public static final int MECAB_EON_NODE = (int)4;
	/// This is a system dictionary.
	public static final int MECAB_SYS_DIC = (int)0;
	/// This is a user dictionary.
	public static final int MECAB_USR_DIC = (int)1;
	/// This is a unknown word dictionary.
	public static final int MECAB_UNK_DIC = (int)2;
	/// One best result is obtained (default mode)
	public static final int MECAB_ONE_BEST = (int)1;
	/// Set this flag if you want to obtain N best results.
	public static final int MECAB_NBEST = (int)2;
	/// Set this flag if you want to enable a partial parsing mode.
	public static final int MECAB_PARTIAL = (int)4;
	/**
	 * Set this flag if you want to obtain marginal probabilities.<br>
	 * Marginal probability is set in MeCab::Node::prob.<br>
	 * The parsing speed will get 3-5 times slower than the default mode.
	 */
	public static final int MECAB_MARGINAL_PROB = (int)8;
	/**
	 * Set this flag if you want to obtain alternative results.<br>
	 * Not implemented.
	 */
	public static final int MECAB_ALTERNATIVE = (int)16;
	/**
	 * When this flag is set, the result linked-list (Node::next/prev)<br>
	 * traverses all nodes in the lattice.
	 */
	public static final int MECAB_ALL_MORPHS = (int)32;
	/**
	 * When this flag is set, tagger internally copies the body of passed<br>
	 * sentence into internal buffer.
	 */
	public static final int MECAB_ALLOCATE_SENTENCE = (int)64;
	/**
	 * C wrapper of MeCab::Tagger::create(argc, argv)<br>
	 * Original signature : <code>mecab_t* mecab_new(int, char**)</code><br>
	 * <i>native declaration : line 347</i>
	 */
	public static Pointer<MecabLibrary.mecab_t > mecab_new(int argc, Pointer<Pointer<Byte > > argv) {
		return Pointer.pointerToAddress(mecab_new(argc, Pointer.getPeer(argv)), MecabLibrary.mecab_t.class);
	}
	@Ptr 
	protected native static long mecab_new(int argc, @Ptr long argv);
	/**
	 * C wrapper of MeCab::Tagger::create(arg)<br>
	 * Original signature : <code>mecab_t* mecab_new2(const char*)</code><br>
	 * <i>native declaration : line 352</i>
	 */
	public static Pointer<MecabLibrary.mecab_t > mecab_new2(Pointer<Byte > arg) {
		return Pointer.pointerToAddress(mecab_new2(Pointer.getPeer(arg)), MecabLibrary.mecab_t.class);
	}
	@Ptr 
	protected native static long mecab_new2(@Ptr long arg);
	/**
	 * C wrapper of MeCab::Tagger::version()<br>
	 * Original signature : <code>char* mecab_version()</code><br>
	 * <i>native declaration : line 357</i>
	 */
	public static Pointer<Byte > mecab_version() {
		return Pointer.pointerToAddress(mecab_version$2(), Byte.class);
	}
	@Ptr 
	@Name("mecab_version") 
	protected native static long mecab_version$2();
	/**
	 * C wrapper of MeCab::getLastError()<br>
	 * Original signature : <code>char* mecab_strerror(mecab_t*)</code><br>
	 * <i>native declaration : line 362</i>
	 */
	public static Pointer<Byte > mecab_strerror(Pointer<MecabLibrary.mecab_t > mecab) {
		return Pointer.pointerToAddress(mecab_strerror(Pointer.getPeer(mecab)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_strerror(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::deleteTagger(tagger)<br>
	 * Original signature : <code>void mecab_destroy(mecab_t*)</code><br>
	 * <i>native declaration : line 367</i>
	 */
	public static void mecab_destroy(Pointer<MecabLibrary.mecab_t > mecab) {
		mecab_destroy(Pointer.getPeer(mecab));
	}
	protected native static void mecab_destroy(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::Tagger:set_partial()<br>
	 * Original signature : <code>int mecab_get_partial(mecab_t*)</code><br>
	 * <i>native declaration : line 372</i>
	 */
	public static int mecab_get_partial(Pointer<MecabLibrary.mecab_t > mecab) {
		return mecab_get_partial(Pointer.getPeer(mecab));
	}
	protected native static int mecab_get_partial(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::Tagger::partial()<br>
	 * Original signature : <code>void mecab_set_partial(mecab_t*, int)</code><br>
	 * <i>native declaration : line 377</i>
	 */
	public static void mecab_set_partial(Pointer<MecabLibrary.mecab_t > mecab, int partial) {
		mecab_set_partial(Pointer.getPeer(mecab), partial);
	}
	protected native static void mecab_set_partial(@Ptr long mecab, int partial);
	/**
	 * C wrapper of MeCab::Tagger::theta()<br>
	 * Original signature : <code>float mecab_get_theta(mecab_t*)</code><br>
	 * <i>native declaration : line 382</i>
	 */
	public static float mecab_get_theta(Pointer<MecabLibrary.mecab_t > mecab) {
		return mecab_get_theta(Pointer.getPeer(mecab));
	}
	protected native static float mecab_get_theta(@Ptr long mecab);
	/**
	 * C wrapper of  MeCab::Tagger::set_theta()<br>
	 * Original signature : <code>void mecab_set_theta(mecab_t*, float)</code><br>
	 * <i>native declaration : line 387</i>
	 */
	public static void mecab_set_theta(Pointer<MecabLibrary.mecab_t > mecab, float theta) {
		mecab_set_theta(Pointer.getPeer(mecab), theta);
	}
	protected native static void mecab_set_theta(@Ptr long mecab, float theta);
	/**
	 * C wrapper of MeCab::Tagger::lattice_level()<br>
	 * Original signature : <code>int mecab_get_lattice_level(mecab_t*)</code><br>
	 * <i>native declaration : line 392</i>
	 */
	public static int mecab_get_lattice_level(Pointer<MecabLibrary.mecab_t > mecab) {
		return mecab_get_lattice_level(Pointer.getPeer(mecab));
	}
	protected native static int mecab_get_lattice_level(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::Tagger::set_lattice_level()<br>
	 * Original signature : <code>void mecab_set_lattice_level(mecab_t*, int)</code><br>
	 * <i>native declaration : line 397</i>
	 */
	public static void mecab_set_lattice_level(Pointer<MecabLibrary.mecab_t > mecab, int level) {
		mecab_set_lattice_level(Pointer.getPeer(mecab), level);
	}
	protected native static void mecab_set_lattice_level(@Ptr long mecab, int level);
	/**
	 * C wrapper of MeCab::Tagger::all_morphs()<br>
	 * Original signature : <code>int mecab_get_all_morphs(mecab_t*)</code><br>
	 * <i>native declaration : line 402</i>
	 */
	public static int mecab_get_all_morphs(Pointer<MecabLibrary.mecab_t > mecab) {
		return mecab_get_all_morphs(Pointer.getPeer(mecab));
	}
	protected native static int mecab_get_all_morphs(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::Tagger::set_all_moprhs()<br>
	 * Original signature : <code>void mecab_set_all_morphs(mecab_t*, int)</code><br>
	 * <i>native declaration : line 407</i>
	 */
	public static void mecab_set_all_morphs(Pointer<MecabLibrary.mecab_t > mecab, int all_morphs) {
		mecab_set_all_morphs(Pointer.getPeer(mecab), all_morphs);
	}
	protected native static void mecab_set_all_morphs(@Ptr long mecab, int all_morphs);
	/**
	 * C wrapper of MeCab::Tagger::parse(MeCab::Lattice *lattice)<br>
	 * Original signature : <code>int mecab_parse_lattice(mecab_t*, mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 412</i>
	 */
	public static int mecab_parse_lattice(Pointer<MecabLibrary.mecab_t > mecab, Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_parse_lattice(Pointer.getPeer(mecab), Pointer.getPeer(lattice));
	}
	protected native static int mecab_parse_lattice(@Ptr long mecab, @Ptr long lattice);
	/**
	 * C wrapper of MeCab::Tagger::parse(const char *str)<br>
	 * Original signature : <code>char* mecab_sparse_tostr(mecab_t*, const char*)</code><br>
	 * <i>native declaration : line 417</i>
	 */
	public static Pointer<Byte > mecab_sparse_tostr(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > str) {
		return Pointer.pointerToAddress(mecab_sparse_tostr(Pointer.getPeer(mecab), Pointer.getPeer(str)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_sparse_tostr(@Ptr long mecab, @Ptr long str);
	/**
	 * C wrapper of MeCab::Tagger::parse(const char *str, size_t len)<br>
	 * Original signature : <code>char* mecab_sparse_tostr2(mecab_t*, const char*, size_t)</code><br>
	 * <i>native declaration : line 422</i>
	 */
	public static Pointer<Byte > mecab_sparse_tostr2(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > str, @Ptr long len) {
		return Pointer.pointerToAddress(mecab_sparse_tostr2(Pointer.getPeer(mecab), Pointer.getPeer(str), len), Byte.class);
	}
	@Ptr 
	protected native static long mecab_sparse_tostr2(@Ptr long mecab, @Ptr long str, @Ptr long len);
	/**
	 * C wrapper of MeCab::Tagger::parse(const char *str, char *ostr, size_t olen)<br>
	 * Original signature : <code>char* mecab_sparse_tostr3(mecab_t*, const char*, size_t, char*, size_t)</code><br>
	 * <i>native declaration : line 427</i>
	 */
	public static Pointer<Byte > mecab_sparse_tostr3(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > str, @Ptr long len, Pointer<Byte > ostr, @Ptr long olen) {
		return Pointer.pointerToAddress(mecab_sparse_tostr3(Pointer.getPeer(mecab), Pointer.getPeer(str), len, Pointer.getPeer(ostr), olen), Byte.class);
	}
	@Ptr 
	protected native static long mecab_sparse_tostr3(@Ptr long mecab, @Ptr long str, @Ptr long len, @Ptr long ostr, @Ptr long olen);
	/**
	 * C wrapper of MeCab::Tagger::parseToNode(const char *str)<br>
	 * Original signature : <code>mecab_node_t* mecab_sparse_tonode(mecab_t*, const char*)</code><br>
	 * <i>native declaration : line 433</i>
	 */
	public static Pointer<mecab_node_t > mecab_sparse_tonode(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > charPtr1) {
		return Pointer.pointerToAddress(mecab_sparse_tonode(Pointer.getPeer(mecab), Pointer.getPeer(charPtr1)), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_sparse_tonode(@Ptr long mecab, @Ptr long charPtr1);
	/**
	 * C wrapper of MeCab::Tagger::parseToNode(const char *str, size_t len)<br>
	 * Original signature : <code>mecab_node_t* mecab_sparse_tonode2(mecab_t*, const char*, size_t)</code><br>
	 * <i>native declaration : line 438</i>
	 */
	public static Pointer<mecab_node_t > mecab_sparse_tonode2(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > charPtr1, @Ptr long size_t1) {
		return Pointer.pointerToAddress(mecab_sparse_tonode2(Pointer.getPeer(mecab), Pointer.getPeer(charPtr1), size_t1), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_sparse_tonode2(@Ptr long mecab, @Ptr long charPtr1, @Ptr long size_t1);
	/**
	 * C wrapper of MeCab::Tagger::parseNBest(size_t N, const char *str)<br>
	 * Original signature : <code>char* mecab_nbest_sparse_tostr(mecab_t*, size_t, const char*)</code><br>
	 * <i>native declaration : line 443</i>
	 */
	public static Pointer<Byte > mecab_nbest_sparse_tostr(Pointer<MecabLibrary.mecab_t > mecab, @Ptr long N, Pointer<Byte > str) {
		return Pointer.pointerToAddress(mecab_nbest_sparse_tostr(Pointer.getPeer(mecab), N, Pointer.getPeer(str)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_nbest_sparse_tostr(@Ptr long mecab, @Ptr long N, @Ptr long str);
	/**
	 * C wrapper of MeCab::Tagger::parseNBest(size_t N, const char *str, size_t len)<br>
	 * Original signature : <code>char* mecab_nbest_sparse_tostr2(mecab_t*, size_t, const char*, size_t)</code><br>
	 * <i>native declaration : line 448</i>
	 */
	public static Pointer<Byte > mecab_nbest_sparse_tostr2(Pointer<MecabLibrary.mecab_t > mecab, @Ptr long N, Pointer<Byte > str, @Ptr long len) {
		return Pointer.pointerToAddress(mecab_nbest_sparse_tostr2(Pointer.getPeer(mecab), N, Pointer.getPeer(str), len), Byte.class);
	}
	@Ptr 
	protected native static long mecab_nbest_sparse_tostr2(@Ptr long mecab, @Ptr long N, @Ptr long str, @Ptr long len);
	/**
	 * C wrapper of MeCab::Tagger::parseNBest(size_t N, const char *str, char *ostr, size_t olen)<br>
	 * Original signature : <code>char* mecab_nbest_sparse_tostr3(mecab_t*, size_t, const char*, size_t, char*, size_t)</code><br>
	 * <i>native declaration : line 454</i>
	 */
	public static Pointer<Byte > mecab_nbest_sparse_tostr3(Pointer<MecabLibrary.mecab_t > mecab, @Ptr long N, Pointer<Byte > str, @Ptr long len, Pointer<Byte > ostr, @Ptr long olen) {
		return Pointer.pointerToAddress(mecab_nbest_sparse_tostr3(Pointer.getPeer(mecab), N, Pointer.getPeer(str), len, Pointer.getPeer(ostr), olen), Byte.class);
	}
	@Ptr 
	protected native static long mecab_nbest_sparse_tostr3(@Ptr long mecab, @Ptr long N, @Ptr long str, @Ptr long len, @Ptr long ostr, @Ptr long olen);
	/**
	 * C wrapper of MeCab::Tagger::parseNBestInit(const char *str)<br>
	 * Original signature : <code>int mecab_nbest_init(mecab_t*, const char*)</code><br>
	 * <i>native declaration : line 461</i>
	 */
	public static int mecab_nbest_init(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > str) {
		return mecab_nbest_init(Pointer.getPeer(mecab), Pointer.getPeer(str));
	}
	protected native static int mecab_nbest_init(@Ptr long mecab, @Ptr long str);
	/**
	 * C wrapper of MeCab::Tagger::parseNBestInit(const char *str, size_t len)<br>
	 * Original signature : <code>int mecab_nbest_init2(mecab_t*, const char*, size_t)</code><br>
	 * <i>native declaration : line 466</i>
	 */
	public static int mecab_nbest_init2(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > str, @Ptr long len) {
		return mecab_nbest_init2(Pointer.getPeer(mecab), Pointer.getPeer(str), len);
	}
	protected native static int mecab_nbest_init2(@Ptr long mecab, @Ptr long str, @Ptr long len);
	/**
	 * C wrapper of MeCab::Tagger::next()<br>
	 * Original signature : <code>char* mecab_nbest_next_tostr(mecab_t*)</code><br>
	 * <i>native declaration : line 471</i>
	 */
	public static Pointer<Byte > mecab_nbest_next_tostr(Pointer<MecabLibrary.mecab_t > mecab) {
		return Pointer.pointerToAddress(mecab_nbest_next_tostr(Pointer.getPeer(mecab)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_nbest_next_tostr(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::Tagger::next(char *ostr, size_t olen)<br>
	 * Original signature : <code>char* mecab_nbest_next_tostr2(mecab_t*, char*, size_t)</code><br>
	 * <i>native declaration : line 476</i>
	 */
	public static Pointer<Byte > mecab_nbest_next_tostr2(Pointer<MecabLibrary.mecab_t > mecab, Pointer<Byte > ostr, @Ptr long olen) {
		return Pointer.pointerToAddress(mecab_nbest_next_tostr2(Pointer.getPeer(mecab), Pointer.getPeer(ostr), olen), Byte.class);
	}
	@Ptr 
	protected native static long mecab_nbest_next_tostr2(@Ptr long mecab, @Ptr long ostr, @Ptr long olen);
	/**
	 * C wrapper of MeCab::Tagger::nextNode()<br>
	 * Original signature : <code>mecab_node_t* mecab_nbest_next_tonode(mecab_t*)</code><br>
	 * <i>native declaration : line 481</i>
	 */
	public static Pointer<mecab_node_t > mecab_nbest_next_tonode(Pointer<MecabLibrary.mecab_t > mecab) {
		return Pointer.pointerToAddress(mecab_nbest_next_tonode(Pointer.getPeer(mecab)), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_nbest_next_tonode(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::Tagger::formatNode(const Node *node)<br>
	 * Original signature : <code>char* mecab_format_node(mecab_t*, const mecab_node_t*)</code><br>
	 * <i>native declaration : line 486</i>
	 */
	public static Pointer<Byte > mecab_format_node(Pointer<MecabLibrary.mecab_t > mecab, Pointer<mecab_node_t > node) {
		return Pointer.pointerToAddress(mecab_format_node(Pointer.getPeer(mecab), Pointer.getPeer(node)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_format_node(@Ptr long mecab, @Ptr long node);
	/**
	 * C wrapper of MeCab::Tagger::dictionary_info()<br>
	 * Original signature : <code>mecab_dictionary_info_t* mecab_dictionary_info(mecab_t*)</code><br>
	 * <i>native declaration : line 491</i>
	 */
	public static Pointer<mecab_dictionary_info_t > mecab_dictionary_info(Pointer<MecabLibrary.mecab_t > mecab) {
		return Pointer.pointerToAddress(mecab_dictionary_info(Pointer.getPeer(mecab)), mecab_dictionary_info_t.class);
	}
	@Ptr 
	protected native static long mecab_dictionary_info(@Ptr long mecab);
	/**
	 * C wrapper of MeCab::createLattice()<br>
	 * Original signature : <code>mecab_lattice_t* mecab_lattice_new()</code><br>
	 * <i>native declaration : line 497</i>
	 */
	public static Pointer<MecabLibrary.mecab_lattice_t > mecab_lattice_new() {
		return Pointer.pointerToAddress(mecab_lattice_new$2(), MecabLibrary.mecab_lattice_t.class);
	}
	@Ptr 
	@Name("mecab_lattice_new") 
	protected native static long mecab_lattice_new$2();
	/**
	 * C wrapper of MeCab::deleteLattice(lattice)<br>
	 * Original signature : <code>void mecab_lattice_destroy(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 502</i>
	 */
	public static void mecab_lattice_destroy(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		mecab_lattice_destroy(Pointer.getPeer(lattice));
	}
	protected native static void mecab_lattice_destroy(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::clear()<br>
	 * Original signature : <code>void mecab_lattice_clear(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 507</i>
	 */
	public static void mecab_lattice_clear(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		mecab_lattice_clear(Pointer.getPeer(lattice));
	}
	protected native static void mecab_lattice_clear(@Ptr long lattice);
	/**
	 * Original signature : <code>int mecab_lattice_is_available(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 513</i>
	 */
	public static int mecab_lattice_is_available(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_lattice_is_available(Pointer.getPeer(lattice));
	}
	protected native static int mecab_lattice_is_available(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::bos_node()<br>
	 * Original signature : <code>mecab_node_t* mecab_lattice_get_bos_node(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 518</i>
	 */
	public static Pointer<mecab_node_t > mecab_lattice_get_bos_node(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_get_bos_node(Pointer.getPeer(lattice)), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_lattice_get_bos_node(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::eos_node()<br>
	 * Original signature : <code>mecab_node_t* mecab_lattice_get_eos_node(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 523</i>
	 */
	public static Pointer<mecab_node_t > mecab_lattice_get_eos_node(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_get_eos_node(Pointer.getPeer(lattice)), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_lattice_get_eos_node(@Ptr long lattice);
	/**
	 * Original signature : <code>mecab_node_t** mecab_lattice_get_all_begin_nodes(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 529</i>
	 */
	public static Pointer<Pointer<mecab_node_t > > mecab_lattice_get_all_begin_nodes(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_get_all_begin_nodes(Pointer.getPeer(lattice)), DefaultParameterizedType.paramType(Pointer.class, mecab_node_t.class));
	}
	@Ptr 
	protected native static long mecab_lattice_get_all_begin_nodes(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::end_nodes()<br>
	 * Original signature : <code>mecab_node_t** mecab_lattice_get_all_end_nodes(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 533</i>
	 */
	public static Pointer<Pointer<mecab_node_t > > mecab_lattice_get_all_end_nodes(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_get_all_end_nodes(Pointer.getPeer(lattice)), DefaultParameterizedType.paramType(Pointer.class, mecab_node_t.class));
	}
	@Ptr 
	protected native static long mecab_lattice_get_all_end_nodes(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::begin_nodes(pos)<br>
	 * Original signature : <code>mecab_node_t* mecab_lattice_get_begin_nodes(mecab_lattice_t*, size_t)</code><br>
	 * <i>native declaration : line 538</i>
	 */
	public static Pointer<mecab_node_t > mecab_lattice_get_begin_nodes(Pointer<MecabLibrary.mecab_lattice_t > lattice, @Ptr long pos) {
		return Pointer.pointerToAddress(mecab_lattice_get_begin_nodes(Pointer.getPeer(lattice), pos), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_lattice_get_begin_nodes(@Ptr long lattice, @Ptr long pos);
	/**
	 * C wrapper of MeCab::Lattice::end_nodes(pos)<br>
	 * Original signature : <code>mecab_node_t* mecab_lattice_get_end_nodes(mecab_lattice_t*, size_t)</code><br>
	 * <i>native declaration : line 543</i>
	 */
	public static Pointer<mecab_node_t > mecab_lattice_get_end_nodes(Pointer<MecabLibrary.mecab_lattice_t > lattice, @Ptr long pos) {
		return Pointer.pointerToAddress(mecab_lattice_get_end_nodes(Pointer.getPeer(lattice), pos), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_lattice_get_end_nodes(@Ptr long lattice, @Ptr long pos);
	/**
	 * C wrapper of MeCab::Lattice::sentence()<br>
	 * Original signature : <code>char* mecab_lattice_get_sentence(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 548</i>
	 */
	public static Pointer<Byte > mecab_lattice_get_sentence(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_get_sentence(Pointer.getPeer(lattice)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_lattice_get_sentence(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::set_sentence(sentence)<br>
	 * Original signature : <code>void mecab_lattice_set_sentence(mecab_lattice_t*, const char*)</code><br>
	 * <i>native declaration : line 553</i>
	 */
	public static void mecab_lattice_set_sentence(Pointer<MecabLibrary.mecab_lattice_t > lattice, Pointer<Byte > sentence) {
		mecab_lattice_set_sentence(Pointer.getPeer(lattice), Pointer.getPeer(sentence));
	}
	protected native static void mecab_lattice_set_sentence(@Ptr long lattice, @Ptr long sentence);
	/**
	 * Original signature : <code>void mecab_lattice_set_sentence2(mecab_lattice_t*, const char*, size_t)</code><br>
	 * <i>native declaration : line 559</i>
	 */
	public static void mecab_lattice_set_sentence2(Pointer<MecabLibrary.mecab_lattice_t > lattice, Pointer<Byte > sentence, @Ptr long len) {
		mecab_lattice_set_sentence2(Pointer.getPeer(lattice), Pointer.getPeer(sentence), len);
	}
	protected native static void mecab_lattice_set_sentence2(@Ptr long lattice, @Ptr long sentence, @Ptr long len);
	/**
	 * C wrapper of MeCab::Lattice::size()<br>
	 * Original signature : <code>size_t mecab_lattice_get_size(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 564</i>
	 */
	@Ptr 
	public static long mecab_lattice_get_size(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_lattice_get_size(Pointer.getPeer(lattice));
	}
	@Ptr 
	protected native static long mecab_lattice_get_size(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::Z()<br>
	 * Original signature : <code>double mecab_lattice_get_z(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 569</i>
	 */
	public static double mecab_lattice_get_z(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_lattice_get_z(Pointer.getPeer(lattice));
	}
	protected native static double mecab_lattice_get_z(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::set_Z()<br>
	 * Original signature : <code>void mecab_lattice_set_z(mecab_lattice_t*, double)</code><br>
	 * <i>native declaration : line 574</i>
	 */
	public static void mecab_lattice_set_z(Pointer<MecabLibrary.mecab_lattice_t > lattice, double Z) {
		mecab_lattice_set_z(Pointer.getPeer(lattice), Z);
	}
	protected native static void mecab_lattice_set_z(@Ptr long lattice, double Z);
	/**
	 * C wrapper of MeCab::Lattice::theta()<br>
	 * Original signature : <code>double mecab_lattice_get_theta(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 579</i>
	 */
	public static double mecab_lattice_get_theta(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_lattice_get_theta(Pointer.getPeer(lattice));
	}
	protected native static double mecab_lattice_get_theta(@Ptr long lattice);
	/**
	 * Original signature : <code>void mecab_lattice_set_theta(mecab_lattice_t*, double)</code><br>
	 * <i>native declaration : line 585</i>
	 */
	public static void mecab_lattice_set_theta(Pointer<MecabLibrary.mecab_lattice_t > lattice, double theta) {
		mecab_lattice_set_theta(Pointer.getPeer(lattice), theta);
	}
	protected native static void mecab_lattice_set_theta(@Ptr long lattice, double theta);
	/**
	 * C wrapper of MeCab::Lattice::next()<br>
	 * Original signature : <code>int mecab_lattice_next(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 590</i>
	 */
	public static int mecab_lattice_next(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_lattice_next(Pointer.getPeer(lattice));
	}
	protected native static int mecab_lattice_next(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::request_type()<br>
	 * Original signature : <code>int mecab_lattice_get_request_type(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 595</i>
	 */
	public static int mecab_lattice_get_request_type(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return mecab_lattice_get_request_type(Pointer.getPeer(lattice));
	}
	protected native static int mecab_lattice_get_request_type(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::has_request_type()<br>
	 * Original signature : <code>int mecab_lattice_has_request_type(mecab_lattice_t*, int)</code><br>
	 * <i>native declaration : line 600</i>
	 */
	public static int mecab_lattice_has_request_type(Pointer<MecabLibrary.mecab_lattice_t > lattice, int request_type) {
		return mecab_lattice_has_request_type(Pointer.getPeer(lattice), request_type);
	}
	protected native static int mecab_lattice_has_request_type(@Ptr long lattice, int request_type);
	/**
	 * C wrapper of MeCab::Lattice::set_request_type()<br>
	 * Original signature : <code>void mecab_lattice_set_request_type(mecab_lattice_t*, int)</code><br>
	 * <i>native declaration : line 605</i>
	 */
	public static void mecab_lattice_set_request_type(Pointer<MecabLibrary.mecab_lattice_t > lattice, int request_type) {
		mecab_lattice_set_request_type(Pointer.getPeer(lattice), request_type);
	}
	protected native static void mecab_lattice_set_request_type(@Ptr long lattice, int request_type);
	/**
	 * Original signature : <code>void mecab_lattice_add_request_type(mecab_lattice_t*, int)</code><br>
	 * <i>native declaration : line 611</i>
	 */
	public static void mecab_lattice_add_request_type(Pointer<MecabLibrary.mecab_lattice_t > lattice, int request_type) {
		mecab_lattice_add_request_type(Pointer.getPeer(lattice), request_type);
	}
	protected native static void mecab_lattice_add_request_type(@Ptr long lattice, int request_type);
	/**
	 * C wrapper of MeCab::Lattice::remove_request_type()<br>
	 * Original signature : <code>void mecab_lattice_remove_request_type(mecab_lattice_t*, int)</code><br>
	 * <i>native declaration : line 616</i>
	 */
	public static void mecab_lattice_remove_request_type(Pointer<MecabLibrary.mecab_lattice_t > lattice, int request_type) {
		mecab_lattice_remove_request_type(Pointer.getPeer(lattice), request_type);
	}
	protected native static void mecab_lattice_remove_request_type(@Ptr long lattice, int request_type);
	/**
	 * C wrapper of MeCab::Lattice::newNode();<br>
	 * Original signature : <code>mecab_node_t* mecab_lattice_new_node(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 621</i>
	 */
	public static Pointer<mecab_node_t > mecab_lattice_new_node(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_new_node(Pointer.getPeer(lattice)), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_lattice_new_node(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::toString()<br>
	 * Original signature : <code>char* mecab_lattice_tostr(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 626</i>
	 */
	public static Pointer<Byte > mecab_lattice_tostr(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_tostr(Pointer.getPeer(lattice)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_lattice_tostr(@Ptr long lattice);
	/**
	 * C wrapper of MeCab::Lattice::toString(buf, size)<br>
	 * Original signature : <code>char* mecab_lattice_tostr2(mecab_lattice_t*, char*, size_t)</code><br>
	 * <i>native declaration : line 631</i>
	 */
	public static Pointer<Byte > mecab_lattice_tostr2(Pointer<MecabLibrary.mecab_lattice_t > lattice, Pointer<Byte > buf, @Ptr long size) {
		return Pointer.pointerToAddress(mecab_lattice_tostr2(Pointer.getPeer(lattice), Pointer.getPeer(buf), size), Byte.class);
	}
	@Ptr 
	protected native static long mecab_lattice_tostr2(@Ptr long lattice, @Ptr long buf, @Ptr long size);
	/**
	 * C wrapper of MeCab::Lattice::enumNBestAsString(N)<br>
	 * Original signature : <code>char* mecab_lattice_nbest_tostr(mecab_lattice_t*, size_t)</code><br>
	 * <i>native declaration : line 636</i>
	 */
	public static Pointer<Byte > mecab_lattice_nbest_tostr(Pointer<MecabLibrary.mecab_lattice_t > lattice, @Ptr long N) {
		return Pointer.pointerToAddress(mecab_lattice_nbest_tostr(Pointer.getPeer(lattice), N), Byte.class);
	}
	@Ptr 
	protected native static long mecab_lattice_nbest_tostr(@Ptr long lattice, @Ptr long N);
	/**
	 * Original signature : <code>char* mecab_lattice_nbest_tostr2(mecab_lattice_t*, size_t, char*, size_t)</code><br>
	 * <i>native declaration : line 642</i>
	 */
	public static Pointer<Byte > mecab_lattice_nbest_tostr2(Pointer<MecabLibrary.mecab_lattice_t > lattice, @Ptr long N, Pointer<Byte > buf, @Ptr long size) {
		return Pointer.pointerToAddress(mecab_lattice_nbest_tostr2(Pointer.getPeer(lattice), N, Pointer.getPeer(buf), size), Byte.class);
	}
	@Ptr 
	protected native static long mecab_lattice_nbest_tostr2(@Ptr long lattice, @Ptr long N, @Ptr long buf, @Ptr long size);
	/**
	 * C wrapper of MeCab::Lattice::what()<br>
	 * Original signature : <code>char* mecab_lattice_strerror(mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 647</i>
	 */
	public static Pointer<Byte > mecab_lattice_strerror(Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_lattice_strerror(Pointer.getPeer(lattice)), Byte.class);
	}
	@Ptr 
	protected native static long mecab_lattice_strerror(@Ptr long lattice);
	/**
	 * C wapper of MeCab::Model::create(argc, argv)<br>
	 * Original signature : <code>mecab_model_t* mecab_model_new(int, char**)</code><br>
	 * <i>native declaration : line 654</i>
	 */
	public static Pointer<MecabLibrary.mecab_model_t > mecab_model_new(int argc, Pointer<Pointer<Byte > > argv) {
		return Pointer.pointerToAddress(mecab_model_new(argc, Pointer.getPeer(argv)), MecabLibrary.mecab_model_t.class);
	}
	@Ptr 
	protected native static long mecab_model_new(int argc, @Ptr long argv);
	/**
	 * C wapper of MeCab::Model::create(arg)<br>
	 * Original signature : <code>mecab_model_t* mecab_model_new2(const char*)</code><br>
	 * <i>native declaration : line 659</i>
	 */
	public static Pointer<MecabLibrary.mecab_model_t > mecab_model_new2(Pointer<Byte > arg) {
		return Pointer.pointerToAddress(mecab_model_new2(Pointer.getPeer(arg)), MecabLibrary.mecab_model_t.class);
	}
	@Ptr 
	protected native static long mecab_model_new2(@Ptr long arg);
	/**
	 * Original signature : <code>void mecab_model_destroy(mecab_model_t*)</code><br>
	 * <i>native declaration : line 665</i>
	 */
	public static void mecab_model_destroy(Pointer<MecabLibrary.mecab_model_t > model) {
		mecab_model_destroy(Pointer.getPeer(model));
	}
	protected native static void mecab_model_destroy(@Ptr long model);
	/**
	 * C wapper of MeCab::Model::createTagger()<br>
	 * Original signature : <code>mecab_t* mecab_model_new_tagger(mecab_model_t*)</code><br>
	 * <i>native declaration : line 670</i>
	 */
	public static Pointer<MecabLibrary.mecab_t > mecab_model_new_tagger(Pointer<MecabLibrary.mecab_model_t > model) {
		return Pointer.pointerToAddress(mecab_model_new_tagger(Pointer.getPeer(model)), MecabLibrary.mecab_t.class);
	}
	@Ptr 
	protected native static long mecab_model_new_tagger(@Ptr long model);
	/**
	 * C wapper of MeCab::Model::createLattice()<br>
	 * Original signature : <code>mecab_lattice_t* mecab_model_new_lattice(mecab_model_t*)</code><br>
	 * <i>native declaration : line 675</i>
	 */
	public static Pointer<MecabLibrary.mecab_lattice_t > mecab_model_new_lattice(Pointer<MecabLibrary.mecab_model_t > model) {
		return Pointer.pointerToAddress(mecab_model_new_lattice(Pointer.getPeer(model)), MecabLibrary.mecab_lattice_t.class);
	}
	@Ptr 
	protected native static long mecab_model_new_lattice(@Ptr long model);
	/**
	 * C wrapper of MeCab::Model::swap()<br>
	 * Original signature : <code>int mecab_model_swap(mecab_model_t*, mecab_model_t*)</code><br>
	 * <i>native declaration : line 680</i>
	 */
	public static int mecab_model_swap(Pointer<MecabLibrary.mecab_model_t > model, Pointer<MecabLibrary.mecab_model_t > new_model) {
		return mecab_model_swap(Pointer.getPeer(model), Pointer.getPeer(new_model));
	}
	protected native static int mecab_model_swap(@Ptr long model, @Ptr long new_model);
	/**
	 * C wapper of MeCab::Model::dictionary_info()<br>
	 * Original signature : <code>mecab_dictionary_info_t* mecab_model_dictionary_info(mecab_model_t*)</code><br>
	 * <i>native declaration : line 685</i>
	 */
	public static Pointer<mecab_dictionary_info_t > mecab_model_dictionary_info(Pointer<MecabLibrary.mecab_model_t > model) {
		return Pointer.pointerToAddress(mecab_model_dictionary_info(Pointer.getPeer(model)), mecab_dictionary_info_t.class);
	}
	@Ptr 
	protected native static long mecab_model_dictionary_info(@Ptr long model);
	/**
	 * C wrapper of MeCab::Model::transition_cost()<br>
	 * Original signature : <code>int mecab_model_transition_cost(mecab_model_t*, unsigned short, unsigned short)</code><br>
	 * <i>native declaration : line 690</i>
	 */
	public static int mecab_model_transition_cost(Pointer<MecabLibrary.mecab_model_t > model, short rcAttr, short lcAttr) {
		return mecab_model_transition_cost(Pointer.getPeer(model), rcAttr, lcAttr);
	}
	protected native static int mecab_model_transition_cost(@Ptr long model, short rcAttr, short lcAttr);
	/**
	 * C wrapper of MeCab::Model::lookup()<br>
	 * Original signature : <code>mecab_node_t* mecab_model_lookup(mecab_model_t*, const char*, const char*, mecab_lattice_t*)</code><br>
	 * <i>native declaration : line 697</i>
	 */
	public static Pointer<mecab_node_t > mecab_model_lookup(Pointer<MecabLibrary.mecab_model_t > model, Pointer<Byte > begin, Pointer<Byte > end, Pointer<MecabLibrary.mecab_lattice_t > lattice) {
		return Pointer.pointerToAddress(mecab_model_lookup(Pointer.getPeer(model), Pointer.getPeer(begin), Pointer.getPeer(end), Pointer.getPeer(lattice)), mecab_node_t.class);
	}
	@Ptr 
	protected native static long mecab_model_lookup(@Ptr long model, @Ptr long begin, @Ptr long end, @Ptr long lattice);
	/**
	 * static functions<br>
	 * Original signature : <code>int mecab_do(int, char**)</code><br>
	 * <i>native declaration : line 703</i>
	 */
	public static int mecab_do(int argc, Pointer<Pointer<Byte > > argv) {
		return mecab_do(argc, Pointer.getPeer(argv));
	}
	protected native static int mecab_do(int argc, @Ptr long argv);
	/**
	 * Original signature : <code>int mecab_dict_index(int, char**)</code><br>
	 * <i>native declaration : line 704</i>
	 */
	public static int mecab_dict_index(int argc, Pointer<Pointer<Byte > > argv) {
		return mecab_dict_index(argc, Pointer.getPeer(argv));
	}
	protected native static int mecab_dict_index(int argc, @Ptr long argv);
	/**
	 * Original signature : <code>int mecab_dict_gen(int, char**)</code><br>
	 * <i>native declaration : line 705</i>
	 */
	public static int mecab_dict_gen(int argc, Pointer<Pointer<Byte > > argv) {
		return mecab_dict_gen(argc, Pointer.getPeer(argv));
	}
	protected native static int mecab_dict_gen(int argc, @Ptr long argv);
	/**
	 * Original signature : <code>int mecab_cost_train(int, char**)</code><br>
	 * <i>native declaration : line 706</i>
	 */
	public static int mecab_cost_train(int argc, Pointer<Pointer<Byte > > argv) {
		return mecab_cost_train(argc, Pointer.getPeer(argv));
	}
	protected native static int mecab_cost_train(int argc, @Ptr long argv);
	/**
	 * Original signature : <code>int mecab_system_eval(int, char**)</code><br>
	 * <i>native declaration : line 707</i>
	 */
	public static int mecab_system_eval(int argc, Pointer<Pointer<Byte > > argv) {
		return mecab_system_eval(argc, Pointer.getPeer(argv));
	}
	protected native static int mecab_system_eval(int argc, @Ptr long argv);
	/**
	 * Original signature : <code>int mecab_test_gen(int, char**)</code><br>
	 * <i>native declaration : line 708</i>
	 */
	public static int mecab_test_gen(int argc, Pointer<Pointer<Byte > > argv) {
		return mecab_test_gen(argc, Pointer.getPeer(argv));
	}
	protected native static int mecab_test_gen(int argc, @Ptr long argv);


	public static class mecab_t extends TypedPointer {
		public mecab_t(long address) {
			super(address);
		}
		public mecab_t(Pointer address) {
			super(address);
		}
	};
	public static class mecab_lattice_t extends TypedPointer {
		public mecab_lattice_t(long address) {
			super(address);
		}
		public mecab_lattice_t(Pointer address) {
			super(address);
		}
	};
	public static class mecab_model_t extends TypedPointer {
		public mecab_model_t(long address) {
			super(address);
		}
		public mecab_model_t(Pointer address) {
			super(address);
		}
	};
}