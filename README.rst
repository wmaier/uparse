=============================================================
 uparse - a shift-reduce parser for discontinuous structures
=============================================================

.. image:: http://www.wolfgang-maier.net/fserv/misc/hhu-small.png
   :align: right
   :alt: HHU Logo

``uparse`` is a shift-reduce parser for discontinuous structures. It has been developed at the project *Grammar Formalisms beyond Context-Free Grammars and their use for Machine Learning Tasks* at the Department for Computational Linguistics at the Institute for Language and Information at the University of Düsseldorf, Germany (see http://phil.hhu.de/beyond-cfg). The project is sponsored by Deutsche Forschungsgemeinschaft (DFG).

If you have questions or comments, please contact Wolfgang Maier
(maierw@hhu.de).

.. contents::


Quick Start
===========

To build the parser, you need at least Java 8 and Apache ant 1.8 or higher. An ant build file is included, the default target of which builds a jar file in the project root. All necessary dependencies are pulled automatically. To run the parser, first generate a parameter file using ``java -jar uparse.jar -generate``. The generated file ``parameters`` contains all available options, set to their default values. Each option is documented. The parser is run by passing it the path to the parameter file, i.e., using ``java -jar uparse.jar parameters``.


Reference
=========

In order to reference this parser, please cite

  Wolfgang Maier (2015):  *Discontinuous incremental shift-reduce                                                                    
  parsing.* In: Proceedings of the 53rd Annual Meeting of the
  Association for Computational Linguistics and the 7th International
  Joint Conference on Natural Language Processing (Volume 1: Long
  Papers), pp. 1202-1212. Beijing, China.


License
=======

The parser code is released under the GNU General Public Licence (GPL) 3.0 or higher. In the build process, *autojar* is used (see http://autojar.sourceforge.net ) (GPL 2). The parser uses the library *fastutil* (http://fastutil.di.unimi.it/ ) (Apache License 2.0). For the full licenses see http://www.gnu.org/licenses/gpl-2.0 , http://www.gnu.org/licenses/gpl-3.0 , and http://www.apache.org/licenses/LICENSE-2.0.html.
