package carueda

package object cfg {
  def $ : Nothing = throw new NotImplementedError

  type SizeInBytes = Long
}
