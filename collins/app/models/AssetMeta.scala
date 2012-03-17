package models

import play.api.libs.json._

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{Schema, Table}

case class AssetMeta(
    name: String,
    priority: Int,
    label: String,
    description: String,
    id: Long = 0) extends ValidatedEntity[Long]
{
  override def validate() {
    require(name != null && name.toUpperCase == name && name.size > 0, "Name must be all upper case, length > 0")
    require(description != null && description.length > 0, "Need a description")
  }
  override def asJson: String = {
    Json.stringify(JsObject(Seq(
      "ID" -> JsNumber(id),
      "NAME" -> JsString(name),
      "PRIORITY" -> JsNumber(priority),
      "LABEL" -> JsString(label),
      "DESCRIPTION" -> JsString(description)
    )))
  }
  def getId(): Long = id
}

object AssetMeta extends Schema with AnormAdapter[AssetMeta] {

  override val tableDef = table[AssetMeta]("asset_meta")
  on(tableDef)(a => declare(
    a.id is(autoIncremented,primaryKey),
    a.name is(unique),
    a.priority is(indexed)
  ))

  override def cacheKeys(a: AssetMeta) = Seq(
    "AssetMeta.findByName(%s)".format(a.name),
    "AssetMeta.findById(%d)".format(a.id),
    "AssetMeta.findAll",
    "AssetMeta.getViewable"
  )
  override def delete(a: AssetMeta): Int = inTransaction {
    afterDeleteCallback(a) {
      tableDef.deleteWhere(p => p.id === a.id)
    }
  }

  def findAll(): Seq[AssetMeta] = getOrElseUpdate("AssetMeta.findAll") {
    from(tableDef)(s => select(s)).toList
  }

  def findById(id: Long) = getOrElseUpdate("AssetMeta.findById(%d)".format(id)) {
    tableDef.lookup(id)
  }

  override def get(a: AssetMeta) = findById(a.id).get

  def findByName(name: String): Option[AssetMeta] = {
    getOrElseUpdate("AssetMeta.findByName(%s)".format(name.toUpperCase)) {
      tableDef.where(a =>
        a.name.toUpperCase === name.toUpperCase
      ).headOption
    }
  }

  def getViewable(): Seq[AssetMeta] = getOrElseUpdate("AssetMeta.getViewable") {
    from(tableDef)(a =>
      where(a.priority gt -1)
      select(a)
      orderBy(a.priority asc)
    ).toList
  }

  type Enum = Enum.Value
  object Enum extends Enumeration(1) {
    val ServiceTag = Value(1, "SERVICE_TAG")
    val ChassisTag = Value(2, "CHASSIS_TAG")
    val RackPosition = Value(3, "RACK_POSITION")
    val PowerPort = Value(4, "POWER_PORT")
    //val SwitchPort = Value(5, "SWITCH_PORT") Deprecated by id LldpPortIdValue

    val CpuCount = Value(6, "CPU_COUNT")
    val CpuCores = Value(7, "CPU_CORES")
    val CpuThreads = Value(8, "CPU_THREADS")
    val CpuSpeedGhz = Value(9, "CPU_SPEED_GHZ")
    val CpuDescription = Value(10, "CPU_DESCRIPTION")

    val MemorySizeBytes = Value(11, "MEMORY_SIZE_BYTES")
    val MemoryDescription = Value(12, "MEMORY_DESCRIPTION")
    val MemorySizeTotal = Value(13, "MEMORY_SIZE_TOTAL")
    val MemoryBanksTotal = Value(14, "MEMORY_BANKS_TOTAL")

    val NicSpeed = Value(15, "NIC_SPEED") // in bits
    val MacAddress = Value(16, "MAC_ADDRESS")
    val NicDescription = Value(17, "NIC_DESCRIPTION")

    val DiskSizeBytes = Value(18, "DISK_SIZE_BYTES")
    val DiskType = Value(19, "DISK_TYPE")
    val DiskDescription = Value(20, "DISK_DESCRIPTION")
    val DiskStorageTotal = Value(21, "DISK_STORAGE_TOTAL")

    val LldpInterfaceName = Value(22, "LLDP_INTERFACE_NAME")
    val LldpChassisName = Value(23, "LLDP_CHASSIS_NAME")
    val LldpChassisIdType = Value(24, "LLDP_CHASSIS_ID_TYPE")
    val LldpChassisIdValue = Value(25, "LLDP_CHASSIS_ID_VALUE")
    val LldpChassisDescription = Value(26, "LLDP_CHASSIS_DESCRIPTION")
    val LldpPortIdType = Value(27, "LLDP_PORT_ID_TYPE")
    val LldpPortIdValue = Value(28, "LLDP_PORT_ID_VALUE")
    val LldpPortDescription = Value(29, "LLDP_PORT_DESCRIPTION")
    val LldpVlanId = Value(30, "LLDP_VLAN_ID")
    val LldpVlanName = Value(31, "LLDP_VLAN_NAME")

    val NicName = Value(32, "INTERFACE_NAME")
    val NicAddress = Value(33, "INTERFACE_ADDRESS")
  }
}
