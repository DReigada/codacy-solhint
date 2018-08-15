package codacy

import codacy.solhint.Solhint
import com.codacy.tools.scala.seed.DockerEngine

object Engine extends DockerEngine(Solhint)()