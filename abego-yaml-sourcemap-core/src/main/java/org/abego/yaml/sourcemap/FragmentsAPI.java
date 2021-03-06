package org.abego.yaml.sourcemap;

import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * The FragmentsAPI provides a fine-grained mapping between the YAML text
 * and its data.
 * 
 * <p>As the name suggests the central idea here are "fragments".</p>
 *
 * <p>Fragments partition the whole YAML text into non-overlapping ranges.
 * Each fragment covers a range of characters that share the same data (address),
 * i.e. the same JSON pointer. Additionally, a fragment is of a certain kind
 * describes what sort of elements in the YAML document a fragment related to.</p>
 *
 * <p>Beside the kinds known from the JSON data model (scalar, sequence, map)
 * other kinds exist that refine these basic kinds, to cover "sub aspects".
 * E.g. for a map the kinds `MAP_KEY` and `MAP_VALUE` define subranges within
 * the map entry's definition.</p>
 *
 * <p>This additional information gives you many options for your applications.
 * E.g. assume you want to use a YAMLSourceMap to implement some content
 * assist feature when editing a YAML document. With the fragments it is easy
 * to display different assists e.g. for the key vs. the value of a map item.
 * Or you can even show different assists depending on where in a map's key
 * the user has the text cursor.</p>
 *
 * <p>The following picture shows the available fragment kinds and how they
 * relate to (a sample) YAML text.</p>
 *
 *  <p><img src="doc-files/fragment-kind-and-legend.png"
 *  alt="Fragment Kinds and how they relate to YAML text"
 *  style="width: 30em"></p>
 *
 * <p>Typically, a given JSON pointer relates to multiple fragments,
 * of different kinds. E.g. in the picture above the first three
 * green fragments all map to JSON pointer {@code /invoice}.</p>
 */
public interface FragmentsAPI {
    /**
     * A range of characters in a YAML document's text that shares the same
     * JSON Pointer and same {@link Kind}.
     *
     * <p>A Fragment contains all characters of the YAML document starting at
     * {@link #getStartOffset()} and ending before {@link #getEndOffset()}.
     * Alternatively the range is also described by (line:column) pairs (see
     * {@link #getStartLine()}:{@link #getStartColumn()} and
     * {@link #getEndLine()}:{@link #getEndColumn()})</p>
     */
    interface Fragment {

        /**
         * The kind of a YAML fragment tells what sort of elements in the YAML
         * document a fragment related to.
         *
         * <p>Beside the kinds known from the JSON data model
         * ({@link Kind#SCALAR}, {@link Kind#SEQUENCE}, {@link Kind#MAP})
         * other kinds exist that refine these basic kinds, to cover "sub aspects". E.g. for
         * a map the kinds {@link Kind#MAP_KEY} and {@link Kind#MAP_VALUE}
         * define subranges within the map entry's definition.</p>
         *
         * <p>The kinds {@link Kind#SCALAR_VALUE}, {@link Kind#SEQUENCE_ITEM},
         * {@link Kind#MAP_VALUE} and {@link Kind#ALIAS_AS_MAP_VALUE} denote
         * fragments that cover a <em>value</em> in the YAML/JSON document.
         * Those aspects of the document relate best to the JSON Pointer
         * definition as "a reference to a <em>value</em> within a JSON
         * document" [1].</p>
         *
         * <p>To take care of 'aliases', a feature of YAML not present in JSON,
         * the "{@code ALIAS_...} kinds are introduced. There are multiple of
         * them to also tell <em>where</em> an alias is used
         * ({@link Kind#ALIAS_AS_SEQUENCE_ITEM}, {@link Kind#ALIAS_AS_MAP_KEY},
         * {@link Kind#ALIAS_AS_MAP_VALUE}).</p>
         *
         * <p>[1]: <a href="https://tools.ietf.org/html/rfc6901"
         * >https://tools.ietf.org/html/rfc6901</a></p>
         */
        enum Kind {
            /**
             * An alias/reference used as a sequence item
             */
            ALIAS_AS_SEQUENCE_ITEM,

            /**
             * An alias/reference used as a map key.
             */
            ALIAS_AS_MAP_KEY,

            /**
             * An alias/reference used as a map value
             */
            ALIAS_AS_MAP_VALUE,

            /**
             * The part from the end of the last element definition to the
             * end of the document.
             *
             * <p> This contains "...", whitespaces and comments.
             */
            DOCUMENT_END,

            /**
             * The part from the start of the YAML document up to the first
             * element definition.
             *
             * <p> This contains "---", whitespaces and comments.
             */
            DOCUMENT_START,

            /**
             * A part of a scalar definition not contributing to the
             * scalar definition's value.
             *
             * <p> This contains whitespaces and comments.
             */
            SCALAR,

            /**
             * A scalar value, like an int, string, boolean etc.
             */
            SCALAR_VALUE,

            /**
             * A part of a sequence (/list) definition not contributing to a
             * sequence item's value.
             *
             * <p> This contains ",", "-", "[", "]", whitespaces, comments.
             */
            SEQUENCE,

            /**
             * A fragment holding the scalar value of an item in a sequence.
             *
             * <p>For non-scalar values the corresponding kinds (sequence, map)
             * are used for the item's value, instead of sequenceItem.</p>
             */
            SEQUENCE_ITEM,

            /**
             * A part of a map (/Hash Table) definition not contributing to a
             * map entry's key or value.
             *
             * <p> This contains ":", "{", "}", whitespaces, comments.
             */
            MAP,

            /**
             * A map entry's key (the scalar before the ":").
             */
            MAP_KEY,

            /**
             * A fragment holding the value of an map entry (the stuff
             * behind the ":").
             *
             * <p>For non-scalar values the corresponding kinds (sequence, map)
             * are used for the map entry's value, instead of mapValue.</p>
             */
            MAP_VALUE
        }

        /**
         * Returns the line of this fragment's start, with {@code 1} referring
         * to the first line.
         *
         * @return the line of this fragment's start, with {@code 1} referring
         * to the first line
         */
        int getStartLine();

        /**
         * Returns the column of this fragment's start, with {@code 1} referring
         * to the first column.
         *
         * @return the column of this fragment's start, with {@code 1} referring
         * to the first column
         */
        int getStartColumn();

        /**
         * Returns the offset of this fragment's start relative to the start of
         * the document.
         *
         * @return the offset of this fragment's start relative to the start of
         * the document
         */
        int getStartOffset();

        /**
         * Returns the line of this fragment's end, with {@code 1} referring
         * to the first line.
         *
         * @return the line of this fragment's end, with {@code 1} referring
         * to the first line
         */
        int getEndLine();

        /**
         * Returns the column of this fragment's end, with {@code 1} referring
         * to the first column.
         *
         * @return the column of this fragment's end, with {@code 1} referring
         * to the first column
         */
        int getEndColumn();

        /**
         * Returns the offset of this fragment's end relative to the start of
         * the document.
         *
         * @return the offset of this fragment's end relative to the start of
         * the document
         */
        int getEndOffset();

        /**
         * Returns the kind of this fragment.
         *
         * @return the kind of this fragment
         */
        Kind getKind();

        /**
         * Returns the JSON pointer related to this fragment.
         *
         * <p>In extension to the original definition of a JSON Pointer as a
         * reference to a <em>value</em> within a JSON document [1] here a JSON
         * Pointer may also refer to <em>other text in a JSON/YAML document
         * directly related to the value</em>.</p>
         *
         * <p>E.g. for the key of a map entry or syntactical elements like
         * "{@code :}" fragments exist that have a different kind but share
         * the same JSON pointer with the fragment referring to the value.</p>
         *
         * <p>See also {@link #getKind()}.</p>
         *
         * <p>[1]: <a href="https://tools.ietf.org/html/rfc6901"
         * >https://tools.ietf.org/html/rfc6901</a></p>
         *
         * @return the JSON pointer related to this fragment
         */
        String getJSONPointer();

        /**
         * Returns true when this fragment contains the {@code offset}
         * ({@code offset} is between the fragment's start and end offset);
         * returns false otherwise.
         *
         * @param offset an offset for the start of the YAML document
         * @return true when this fragment contains the {@code offset}
         * ({@code offset} is between the fragment's start and end offset),
         * false otherwise
         */
        default boolean containsOffset(int offset) {
            return offset >= getStartOffset() && offset < getEndOffset();
        }

        /**
         * Returns true when the fragment contains the location defined by
         * {@code line} and {@code column}; returns false otherwise.
         *
         * @param line   the number of a line in the YAML document, with 1 for the
         *               first line
         * @param column the number of a column in the given line,
         *               with 1 for the first column
         * @return true when the fragment contains the location defined by
         * {@code line} and {@code column}, false otherwise
         */
        default boolean containsLocation(int line, int column) {
            if (line < getStartLine() || getEndLine() < line)
                return false;

            boolean leftOK = getStartLine() < line || getStartColumn() <= column;
            boolean rightOK = line < getEndLine() || column < getEndColumn();
            return leftOK && rightOK;
        }
    }

    /**
     * Returns the length of the YAML document.
     *
     * @return the length of the YAML document
     */
    int documentLength();

    /**
     * Returns all {@link Fragment}s of the YAML document.
     *
     * @return all {@link Fragment}s of the YAML document
     */
    List<Fragment> allFragments();

    /**
     * Returns all {@link Fragment}s matching the {@code test}.
     *
     * @param test returns true when the fragment passed in should be included
     *             in the result, false otherwise
     * @return all {@link Fragment}s matching the {@code test}
     */
    List<Fragment> allFragmentsMatching(Predicate<Fragment> test);

    /**
     * Returns the first {@link Fragment} that matching the {@code test};
     * or {@code null} when no fragment matches the test.
     *
     * @param test returns true when the fragment passed in should be returned
     *             as the method's result, false otherwise.
     * @return the first {@link Fragment} that matching the {@code test};
     * or {@code null} when no fragment matches the test
     */
    @Nullable
    Fragment findFirstFragmentMatching(Predicate<Fragment> test);

    /**
     * Returns the {@link Fragment} for the given offset.
     *
     * @param offset an offset for the start of the YAML document
     * @return the {@link Fragment} for the given offset
     */
    Fragment fragmentAtOffset(int offset);

    /**
     * Returns the {@link Fragment} for the given location.
     *
     * @param line   the number of a line in the YAML document, with 1 for the
     *               first line
     * @param column the number of a column in the given line,
     *               with 1 for the first column
     * @return the {@link Fragment} for the given location
     * @throws YAMLSourceMapException when the location is invalid
     */
    Fragment fragmentAtLocation(int line, int column);

    /**
     * Returns all {@link Fragment}s related to the entity
     * referenced by the {@code jsonPointer}.
     *
     * @param jsonPointer A valid JSON Pointer, referring to an entity
     *                    defined in this source map's YAML document.
     * @return all {@link Fragment}s related to the entity referenced by the
     * {@code jsonPointer}
     */
    List<Fragment> allFragmentsOfJsonPointer(String jsonPointer);

    /**
     * Returns all {@link Fragment}s of children of the entity referenced
     * by the {@code jsonPointer}.
     *
     * @param jsonPointer A valid JSON Pointer, referring to an entity
     *                    defined in this source map's YAML document.
     * @return all {@link Fragment}s of children of the entity referenced
     * by the {@code jsonPointer}
     */
    List<Fragment> allFragmentsOfChildrenOfJsonPointer(String jsonPointer);

    /**
     * Returns the {@link Fragment} holding the value of the entity
     * referenced by the {@code jsonPointer}; returns {@code null} when no such
     * fragment exists.
     *
     * <p>For a given JSON Pointer a source map hold multiple fragments.
     * This method returns the fragment holding the <em>value</em>. Depending
     * on the type of the entity different kinds of fragments are used:
     * <ul>
     *     <li><b>Scalar: </b> scalarValue</li>
     *     <li><b>Sequence: </b> sequenceItem  or aliasAsSequenceItem</li>
     *     <li><b>Map: </b> mapValue or aliasAsMapValue</li>
     * </ul>
     * <p>
     * Note: This will only return 'scalar' values. For compound values use
     * the {@link #allFragmentsOfChildrenOfJsonPointer(String)}.
     *
     * @param jsonPointer A valid JSON Pointer, referring to an entity
     *                    defined in this source map's YAML document.
     * @return the {@link Fragment} holding the value of the entity referenced
     * by the {@code jsonPointer}; or {@code null} when no such
     * fragment exists.
     */
    @Nullable
    Fragment valueFragmentOfJsonPointer(String jsonPointer);
}
