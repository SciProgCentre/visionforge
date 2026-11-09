package space.kscience.visionforge.demo.factory.scene

import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.names.Name
import space.kscience.kmath.geometry.euclidean3d.Float32Vector3D
import space.kscience.visionforge.demo.factory.model.EquipmentSpec
import space.kscience.visionforge.demo.factory.model.NozzleSpec
import space.kscience.visionforge.demo.factory.styles.MogStyles
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.useStyle
import kotlin.math.*

internal operator fun Float32Vector3D.plus(other: Float32Vector3D): Float32Vector3D =
    Float32Vector3D(x + other.x, y + other.y, z + other.z)

public data class CalculatedNozzle(
    val spec: NozzleSpec,
    val baseLocal: Float32Vector3D,
    val connLocal: Float32Vector3D,
    val normalLocal: Float32Vector3D,
    val connWorld: Float32Vector3D,
    val normalWorld: Float32Vector3D,
    val equipmentId: String,
)

public object MogEquipmentBuilder {

    public const val STUB: Double = 0.4
    public const val STUB_IN: Double = 0.2

    public fun dnRadius(dnMm: Int): Double {
        val d = dnMm / 1000.0
        return if (dnMm >= 1000) d / 2.0 else (d / 2.0) * 1.1
    }

    public fun extLen(dnMm: Int): Double =
        0.3 + (if (dnMm >= 1000) dnMm / 2000.0 else 0.0)

    public fun rotZ(v: Float32Vector3D, deg: Double): Float32Vector3D {
        val a = Math.toRadians(deg)
        val c = cos(a).toFloat()
        val s = sin(a).toFloat()
        return Float32Vector3D(v.x * c - v.y * s, v.x * s + v.y * c, v.z)
    }

    private fun headZ(eq: EquipmentSpec, rho: Double): Double {
        val r = (eq.d ?: 1.0) / 2.0
        return when (eq.template) {
            "tower" -> {
                val top = (eq.skirtTop ?: 0.0) + (eq.shellH ?: 0.0)
                val clampedRho = min(rho, r * 0.999)
                top + (eq.headH ?: 0.0) * sqrt(1.0 - (clampedRho / r).pow(2))
            }
            "tank", "elevated_tank" -> {
                val top = (eq.base ?: 0.3) + (eq.h ?: 0.0)
                top + (eq.roofH ?: 0.0) * (1.0 - min(rho, r) / r)
            }
            "wesp" -> {
                val z0 = (eq.legsH ?: 0.0) + (eq.inletH ?: 0.0) + (eq.tubeH ?: 0.0)
                z0 + (eq.hoodH ?: 0.0) * (1.0 - min(rho, r) / r)
            }
            "thickener" -> {
                (eq.legsH ?: 0.0) + (eq.wallH ?: 0.0)
            }
            else -> error("Unsupported vessel template: ${eq.template}")
        }
    }

    private fun apexTop(eq: EquipmentSpec): Double {
        return if (eq.template == "wesp") {
            (eq.legsH ?: 0.0) + (eq.inletH ?: 0.0) + (eq.tubeH ?: 0.0) + (eq.hoodH ?: 0.0)
        } else {
            headZ(eq, 0.0)
        }
    }

    private fun bottomZ(eq: EquipmentSpec): Double {
        return when (eq.template) {
            "wesp" -> (eq.legsH ?: 0.0) - (eq.hopperH ?: 0.0)
            "thickener" -> (eq.legsH ?: 0.0) - (eq.coneH ?: 0.0)
            else -> error("Unsupported vessel bottom template: ${eq.template}")
        }
    }

    private fun faceNormal(face: String): Float32Vector3D = when (face) {
        "+x" -> Float32Vector3D(1f, 0f, 0f)
        "-x" -> Float32Vector3D(-1f, 0f, 0f)
        "+y" -> Float32Vector3D(0f, 1f, 0f)
        "-y" -> Float32Vector3D(0f, -1f, 0f)
        "top" -> Float32Vector3D(0f, 0f, 1f)
        "bottom" -> Float32Vector3D(0f, 0f, -1f)
        else -> Float32Vector3D(0f, 0f, 1f)
    }

    public fun calculateNozzles(eq: EquipmentSpec): List<CalculatedNozzle> {
        val r = (eq.d ?: 1.0) / 2.0
        val rz = eq.rotZ ?: 0.0
        val posX = eq.pos.getOrElse(0) { 0.0 }.toFloat()
        val posY = eq.pos.getOrElse(1) { 0.0 }.toFloat()
        val posZ = eq.pos.getOrElse(2) { 0.0 }.toFloat()

        return when (eq.template) {
            "tower", "tank", "elevated_tank", "wesp", "thickener" -> {
                eq.nozzles.map { n ->
                    val rn = dnRadius(n.dn)
                    val nv = faceNormal(n.face)
                    val (base, conn) = when (n.face) {
                        "top" -> {
                            val ox = n.off?.getOrElse(0) { 0.0 } ?: 0.0
                            val oy = n.off?.getOrElse(1) { 0.0 } ?: 0.0
                            val rho = hypot(ox, oy)
                            val zConn = (if (rho < 1e-6) apexTop(eq) else headZ(eq, rho)) + STUB
                            val zBase = headZ(eq, rho + rn) - STUB_IN
                            Float32Vector3D(ox.toFloat(), oy.toFloat(), zBase.toFloat()) to
                                    Float32Vector3D(ox.toFloat(), oy.toFloat(), zConn.toFloat())
                        }
                        "bottom" -> {
                            val zb = bottomZ(eq)
                            Float32Vector3D(0f, 0f, (zb + STUB_IN + 0.3).toFloat()) to
                                    Float32Vector3D(0f, 0f, (zb - STUB).toFloat())
                        }
                        else -> {
                            val t = n.t ?: 0.0
                            val z = (n.z ?: 0.0).toFloat()
                            val s = sqrt(max(r * r - t * t, 0.0)).toFloat()
                            when (n.face) {
                                "+x" -> Float32Vector3D(s - STUB_IN.toFloat(), t.toFloat(), z) to
                                        Float32Vector3D(s + STUB.toFloat(), t.toFloat(), z)
                                "-x" -> Float32Vector3D(-(s - STUB_IN.toFloat()), t.toFloat(), z) to
                                        Float32Vector3D(-(s + STUB.toFloat()), t.toFloat(), z)
                                "+y" -> Float32Vector3D(t.toFloat(), s - STUB_IN.toFloat(), z) to
                                        Float32Vector3D(t.toFloat(), s + STUB.toFloat(), z)
                                else -> Float32Vector3D(t.toFloat(), -(s - STUB_IN.toFloat()), z) to
                                        Float32Vector3D(t.toFloat(), -(s + STUB.toFloat()), z)
                            }
                        }
                    }
                    val connWorld = rotZ(conn, rz) + Float32Vector3D(posX, posY, posZ)
                    val normalWorld = rotZ(nv, rz)
                    CalculatedNozzle(n, base, conn, nv, connWorld, normalWorld, eq.id)
                }
            }
            "pump" -> {
                val small = eq.small ?: false
                val L = if (small) 1.4 else 2.6
                val W = if (small) 0.6 else 1.0
                val bh = if (small) 0.2 else 0.25
                val xc = if (small) -0.25 else -0.7
                val rc = if (small) 0.25 else 0.55
                val wc = if (small) 0.25 else 0.45
                val zs = (bh + rc + 0.1).toFloat()

                val suctionDn = if (small) 80 else 300
                val dischargeDn = if (small) 50 else 250

                val suction = CalculatedNozzle(
                    spec = NozzleSpec("Suction", "liquidIn", suctionDn, "-x"),
                    baseLocal = Float32Vector3D((xc - wc / 2 + STUB_IN).toFloat(), 0f, zs),
                    connLocal = Float32Vector3D((xc - wc / 2 - 0.35).toFloat(), 0f, zs),
                    normalLocal = Float32Vector3D(-1f, 0f, 0f),
                    connWorld = rotZ(Float32Vector3D((xc - wc / 2 - 0.35).toFloat(), 0f, zs), rz) + Float32Vector3D(posX, posY, posZ),
                    normalWorld = rotZ(Float32Vector3D(-1f, 0f, 0f), rz),
                    equipmentId = eq.id
                )
                val discharge = CalculatedNozzle(
                    spec = NozzleSpec("Discharge", "liquidOut", dischargeDn, "top"),
                    baseLocal = Float32Vector3D(xc.toFloat(), 0f, (zs + rc - STUB_IN).toFloat()),
                    connLocal = Float32Vector3D(xc.toFloat(), 0f, (zs + rc + if (small) 0.25 else 0.3).toFloat()),
                    normalLocal = Float32Vector3D(0f, 0f, 1f),
                    connWorld = rotZ(Float32Vector3D(xc.toFloat(), 0f, (zs + rc + if (small) 0.25 else 0.3).toFloat()), rz) + Float32Vector3D(posX, posY, posZ),
                    normalWorld = rotZ(Float32Vector3D(0f, 0f, 1f), rz),
                    equipmentId = eq.id
                )
                listOf(suction, discharge)
            }
            "fan" -> {
                val inlet = CalculatedNozzle(
                    spec = NozzleSpec("Inlet", "gasIn", 200, "-x"),
                    baseLocal = Float32Vector3D(-0.2f, 0f, 0.75f),
                    connLocal = Float32Vector3D(-0.625f, 0f, 0.75f),
                    normalLocal = Float32Vector3D(-1f, 0f, 0f),
                    connWorld = rotZ(Float32Vector3D(-0.625f, 0f, 0.75f), rz) + Float32Vector3D(posX, posY, posZ),
                    normalWorld = rotZ(Float32Vector3D(-1f, 0f, 0f), rz),
                    equipmentId = eq.id
                )
                val outlet = CalculatedNozzle(
                    spec = NozzleSpec("Outlet", "gasOut", 200, "top"),
                    baseLocal = Float32Vector3D(-0.2f, 0f, 1.0f),
                    connLocal = Float32Vector3D(-0.2f, 0f, 1.5f),
                    normalLocal = Float32Vector3D(0f, 0f, 1f),
                    connWorld = rotZ(Float32Vector3D(-0.2f, 0f, 1.5f), rz) + Float32Vector3D(posX, posY, posZ),
                    normalWorld = rotZ(Float32Vector3D(0f, 0f, 1f), rz),
                    equipmentId = eq.id
                )
                listOf(inlet, outlet)
            }
            "phe", "exchanger" -> {
                val L = eq.length ?: 2.4
                val H = eq.height ?: 2.8
                val nozDefs = listOf(
                    Triple("HotIn", "liquidIn", Triple("-x", 0.3, H - 0.4)),
                    Triple("HotOut", "liquidOut", Triple("-x", 0.3, 0.7)),
                    Triple("ColdIn", "liquidIn", Triple("+x", -0.25, 0.7)),
                    Triple("ColdOut", "liquidOut", Triple("+x", 0.25, H - 0.4)),
                )
                nozDefs.map { (nid, port, data) ->
                    val (f, y, z) = data
                    val sg = if (f == "+x") 1.0 else -1.0
                    val dn = if (nid.startsWith("Hot")) 300 else 250
                    val base = Float32Vector3D((sg * (L / 2 - STUB_IN)).toFloat(), y.toFloat(), z.toFloat())
                    val conn = Float32Vector3D((sg * (L / 2 + STUB)).toFloat(), y.toFloat(), z.toFloat())
                    val nv = faceNormal(f)
                    CalculatedNozzle(
                        spec = NozzleSpec(nid, port, dn, f),
                        baseLocal = base,
                        connLocal = conn,
                        normalLocal = nv,
                        connWorld = rotZ(conn, rz) + Float32Vector3D(posX, posY, posZ),
                        normalWorld = rotZ(nv, rz),
                        equipmentId = eq.id
                    )
                }
            }
            "filterpress", "filter_press" -> {
                val pz = eq.platformZ ?: 2.0
                val feed = CalculatedNozzle(
                    spec = NozzleSpec("Feed", "liquidIn", 50, "-x"),
                    baseLocal = Float32Vector3D((-2.2 + STUB_IN).toFloat(), 0f, (pz + 1.0).toFloat()),
                    connLocal = Float32Vector3D(-2.6f, 0f, (pz + 1.0).toFloat()),
                    normalLocal = Float32Vector3D(-1f, 0f, 0f),
                    connWorld = rotZ(Float32Vector3D(-2.6f, 0f, (pz + 1.0).toFloat()), rz) + Float32Vector3D(posX, posY, posZ),
                    normalWorld = rotZ(Float32Vector3D(-1f, 0f, 0f), rz),
                    equipmentId = eq.id
                )
                val filtrate = CalculatedNozzle(
                    spec = NozzleSpec("Filtrate", "liquidOut", 50, "-x"),
                    baseLocal = Float32Vector3D((-2.2 + STUB_IN).toFloat(), 0.45f, (pz + 0.4).toFloat()),
                    connLocal = Float32Vector3D(-2.6f, 0.45f, (pz + 0.4).toFloat()),
                    normalLocal = Float32Vector3D(-1f, 0f, 0f),
                    connWorld = rotZ(Float32Vector3D(-2.6f, 0.45f, (pz + 0.4).toFloat()), rz) + Float32Vector3D(posX, posY, posZ),
                    normalWorld = rotZ(Float32Vector3D(-1f, 0f, 0f), rz),
                    equipmentId = eq.id
                )
                listOf(feed, filtrate)
            }
            else -> emptyList()
        }
    }

    public fun SolidGroup.buildEquipment(
        eq: EquipmentSpec,
        nozzles: List<CalculatedNozzle>,
    ) {
        val eqGroup = solidGroup(eq.id.replace('-', '_')) {
            x = eq.pos.getOrElse(0) { 0.0 }
            y = eq.pos.getOrElse(1) { 0.0 }
            z = eq.pos.getOrElse(2) { 0.0 }
            eq.rotZ?.let { rotationZ = Math.toRadians(it) }

            // Attach DEXPI and Digital Twin metadata
            properties.setValue(Name.of("dexpi:class"), eq.dexpi.asValue())
            properties.setValue(Name.of("dexpi:tagName"), eq.id.asValue())
            properties.setValue(Name.of("dt:nodeId"), eq.id.asValue())
            properties.setValue(Name.of("dt:nameRu"), eq.name.asValue())
            properties.setValue(Name.of("dt:nodeType"), eq.nodeType.asValue())
            properties.setValue(Name.of("dt:state"), eq.state.asValue())
            properties.setValue(Name.of("dt:dataStatus"), (eq.status ?: "T").asValue())

            // Apply default material style
            useStyle(MogStyles.materialStyleName(eq.material))

            val geomGroup = solidGroup("Geom") {
                val r = (eq.d ?: 1.0) / 2.0
                val m = eq.material
                when (eq.template) {
                    "tower" -> {
                        val sk = eq.skirtTop ?: 2.0
                        val shellH = eq.shellH ?: 10.0
                        val headH = eq.headH ?: 1.0

                        cylinder(r = r + 0.3, height = 0.3, name = "Plinth") {
                            z = 0.15
                            useStyle(MogStyles.materialStyleName("Concrete"))
                        }
                        cylinder(r = r * 0.98, height = sk - 0.3, name = "Skirt") {
                            z = 0.3 + (sk - 0.3) / 2.0
                            useStyle(MogStyles.materialStyleName("PaintedSteel"))
                        }
                        cylinder(r = r, height = shellH, name = "Shell") {
                            z = sk + shellH / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        sphere(radius = r, name = "Head") {
                            z = sk + shellH
                            scaleZ = headH / r
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        if (eq.d != null && eq.d >= 3.0) {
                            cylinder(r = r + 0.9, height = 0.12, name = "PlatformTop") {
                                z = sk + shellH - 0.5
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                            cylinder(r = r + 0.9, height = 0.12, name = "PlatformLow") {
                                z = sk - 1.2
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }
                    }
                    "tank", "elevated_tank" -> {
                        val base = eq.base ?: 0.3
                        val h = eq.h ?: 4.0
                        val roofH = eq.roofH ?: 1.0
                        if (eq.template == "tank") {
                            cylinder(r = r + 0.2, height = 0.3, name = "Plinth") {
                                z = 0.15
                                useStyle(MogStyles.materialStyleName("Concrete"))
                            }
                        } else {
                            val a = r + 0.35
                            val fm = eq.frameMaterial ?: "Steel"
                            val signs = listOf(1.0 to 1.0, -1.0 to 1.0, -1.0 to -1.0, 1.0 to -1.0)
                            signs.forEachIndexed { i, (sx, sy) ->
                                box(xSize = 0.3, ySize = 0.3, zSize = base - 0.2, name = "Column_${i + 1}") {
                                    x = sx * a
                                    y = sy * a
                                    z = base / 2.0 - 0.1
                                    useStyle(MogStyles.materialStyleName(fm))
                                }
                            }
                            box(xSize = 2 * a + 0.6, ySize = 2 * a + 0.6, zSize = 0.2, name = "Platform") {
                                z = base - 0.1
                                useStyle(MogStyles.materialStyleName(fm))
                            }
                            listOf(base * 0.35, base * 0.7).forEachIndexed { k, zz ->
                                box(xSize = 2 * a, ySize = 0.2, zSize = 0.25, name = "BeamX_${k + 1}") {
                                    y = a
                                    z = zz
                                    useStyle(MogStyles.materialStyleName(fm))
                                }
                                box(xSize = 0.2, ySize = 2 * a, zSize = 0.25, name = "BeamY_${k + 1}") {
                                    x = a
                                    z = zz
                                    useStyle(MogStyles.materialStyleName(fm))
                                }
                            }
                        }
                        cylinder(r = r, height = h, name = "Shell") {
                            z = base + h / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        cone(bottomRadius = r, height = roofH, upperRadius = 0.0, name = "Roof") {
                            z = base + h + roofH / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                    }
                    "wesp" -> {
                        val lh = eq.legsH ?: 4.0
                        val hh = eq.hopperH ?: 3.0
                        val ih = eq.inletH ?: 2.0
                        val th = eq.tubeH ?: 6.0
                        val hd = eq.hoodH ?: 2.0
                        val a = (r - 0.55) / sqrt(2.0)
                        val signs = listOf(1.0 to 1.0, -1.0 to 1.0, -1.0 to -1.0, 1.0 to -1.0)
                        signs.forEachIndexed { i, (sx, sy) ->
                            box(xSize = 0.35, ySize = 0.35, zSize = lh, name = "Leg_${i + 1}") {
                                x = sx * a
                                y = sy * a
                                z = lh / 2.0
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }
                        cone(bottomRadius = r, height = hh, upperRadius = 0.0, name = "Hopper") {
                            z = lh - hh / 2.0
                            rotationX = PI
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        cylinder(r = r, height = ih, name = "InletSection") {
                            z = lh + ih / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        cylinder(r = r * 0.99, height = th, name = "TubeSection") {
                            z = lh + ih + th / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        val z0 = lh + ih + th
                        cone(bottomRadius = r, height = hd, upperRadius = 0.0, name = "Hood") {
                            z = z0 + hd / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        cylinder(r = r + 0.9, height = 0.12, name = "Platform") {
                            z = z0 - 0.3
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        listOf(45.0, 135.0, 225.0, 315.0).forEachIndexed { i, ang ->
                            val rr = 1.9
                            val rad = Math.toRadians(ang)
                            val xx = rr * cos(rad)
                            val yy = rr * sin(rad)
                            val zc = z0 + hd * (1.0 - rr / r)
                            cylinder(r = 0.4, height = 1.2, name = "InsulatorHousing_${i + 1}") {
                                x = xx
                                y = yy
                                z = zc + 0.5
                                useStyle(MogStyles.materialStyleName(m))
                            }
                        }
                        box(xSize = 1.9, ySize = 1.6, zSize = 0.3, name = "TRBracket") {
                            x = r + 0.75
                            z = z0 - 0.15
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        box(xSize = 1.6, ySize = 1.4, zSize = 1.6, name = "TRSet") {
                            x = r + 0.75
                            z = z0 + 0.8
                            useStyle(MogStyles.materialStyleName("HV"))
                        }
                    }
                    "thickener" -> {
                        val lh = eq.legsH ?: 4.0
                        val wh = eq.wallH ?: 3.0
                        val ch = eq.coneH ?: 2.0
                        val a = (r - 0.4) / sqrt(2.0)
                        val signs = listOf(1.0 to 1.0, -1.0 to 1.0, -1.0 to -1.0, 1.0 to -1.0)
                        signs.forEachIndexed { i, (sx, sy) ->
                            box(xSize = 0.3, ySize = 0.3, zSize = lh, name = "Leg_${i + 1}") {
                                x = sx * a
                                y = sy * a
                                z = lh / 2.0
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }
                        cone(bottomRadius = r, height = ch, upperRadius = 0.0, name = "ConeBottom") {
                            z = lh - ch / 2.0
                            rotationX = PI
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        cylinder(r = r, height = wh, name = "Wall") {
                            z = lh + wh / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = 0.5, ySize = 2 * r + 0.4, zSize = 0.4, name = "Bridge") {
                            z = lh + wh + 0.2
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        box(xSize = 1.0, ySize = 1.0, zSize = 0.8, name = "RakeDrive") {
                            z = lh + wh + 0.8
                            useStyle(MogStyles.materialStyleName("Motor"))
                        }
                    }
                    "pump" -> {
                        val small = eq.small ?: false
                        val L = if (small) 1.4 else 2.6
                        val W = if (small) 0.6 else 1.0
                        val bh = if (small) 0.2 else 0.25
                        val xc = if (small) -0.25 else -0.7
                        val rc = if (small) 0.25 else 0.55
                        val wc = if (small) 0.25 else 0.45
                        val xm = if (small) 0.35 else 0.55
                        val rm = if (small) 0.18 else 0.35
                        val lm = if (small) 0.6 else 1.1
                        val zs = bh + rc + 0.1

                        box(xSize = L, ySize = W, zSize = bh, name = "Baseplate") {
                            z = bh / 2.0
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        cylinder(r = rc, height = wc, name = "Casing") {
                            x = xc
                            z = zs
                            rotationY = PI / 2
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(
                            xSize = abs(xm - lm / 2.0 - xc) - wc / 2.0,
                            ySize = W * 0.45,
                            zSize = rc * 1.1,
                            name = "BearingFrame"
                        ) {
                            x = (xc + xm - lm / 2.0) / 2.0
                            z = zs - rc * 0.2
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        cylinder(r = rm, height = lm, name = "Motor") {
                            x = xm
                            z = zs
                            rotationY = PI / 2
                            useStyle(MogStyles.materialStyleName("Motor"))
                        }
                        box(
                            xSize = lm * 0.8,
                            ySize = rm * 1.6,
                            zSize = zs - rm - bh + 0.04,
                            name = "MotorFeet"
                        ) {
                            x = xm
                            z = (bh + zs - rm) / 2.0 + 0.02
                            useStyle(MogStyles.materialStyleName("Motor"))
                        }
                    }
                    "fan" -> {
                        box(xSize = 1.3, ySize = 0.8, zSize = 0.2, name = "Base") {
                            x = 0.05
                            z = 0.1
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        cylinder(r = 0.45, height = 0.35, name = "Volute") {
                            x = -0.2
                            z = 0.75
                            rotationY = PI / 2
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = 0.35, ySize = 0.3, zSize = 0.35, name = "Pedestal") {
                            x = 0.4
                            z = 0.375
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        cylinder(r = 0.2, height = 0.55, name = "Motor") {
                            x = 0.45
                            z = 0.75
                            rotationY = PI / 2
                            useStyle(MogStyles.materialStyleName("Motor"))
                        }
                    }
                    "phe", "exchanger" -> {
                        val L = eq.length ?: 2.4
                        val W = eq.width ?: 1.0
                        val H = eq.height ?: 2.8

                        box(xSize = 0.2, ySize = W, zSize = H, name = "FixedHead") {
                            x = -L / 2.0 + 0.1
                            z = H / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = 1.56, ySize = W * 0.9, zSize = H * 0.86, name = "PlatePack") {
                            x = -L / 2.0 + 0.2 + 0.78
                            z = H / 2.0 + 0.05
                            useStyle(MogStyles.materialStyleName("SS"))
                        }
                        box(xSize = 0.15, ySize = W * 0.95, zSize = H * 0.9, name = "PressurePlate") {
                            x = -L / 2.0 + 0.2 + 1.56 + 0.08
                            z = H / 2.0 + 0.05
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = 0.16, ySize = 0.35, zSize = H, name = "SupportColumn") {
                            x = L / 2.0 - 0.08
                            z = H / 2.0
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = L, ySize = 0.12, zSize = 0.16, name = "CarryingBar") {
                            z = H + 0.08
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        box(xSize = L, ySize = 0.1, zSize = 0.1, name = "GuideBar") {
                            z = 0.3
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                    }
                    "filterpress", "filter_press" -> {
                        val pz = eq.platformZ ?: 2.0
                        val signs = listOf(1.0 to 1.0, -1.0 to 1.0, -1.0 to -1.0, 1.0 to -1.0)
                        signs.forEachIndexed { i, (sx, sy) ->
                            box(xSize = 0.3, ySize = 0.3, zSize = pz - 0.2, name = "Leg_${i + 1}") {
                                x = sx * 2.3
                                y = sy * 0.95
                                z = (pz - 0.2) / 2.0
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }
                        box(xSize = 5.2, ySize = 2.3, zSize = 0.2, name = "Platform") {
                            z = pz - 0.1
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                        box(xSize = 0.4, ySize = 1.4, zSize = 1.7, name = "FixedHead") {
                            x = -2.0
                            z = pz + 0.85
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = 0.4, ySize = 1.4, zSize = 1.7, name = "EndHead") {
                            x = 1.6
                            z = pz + 0.85
                            useStyle(MogStyles.materialStyleName(m))
                        }
                        box(xSize = 2.9, ySize = 1.2, zSize = 1.3, name = "PlatePack") {
                            x = -0.35
                            z = pz + 0.85
                            useStyle(MogStyles.materialStyleName("PP"))
                        }
                        listOf(0.62, -0.62).forEachIndexed { k, yy ->
                            box(xSize = 3.6, ySize = 0.15, zSize = 0.15, name = "SideBar_${k + 1}") {
                                x = -0.2
                                y = yy
                                z = pz + 1.55
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }
                        cylinder(r = 0.16, height = 1.0, name = "HydraulicCylinder") {
                            x = 2.3
                            z = pz + 0.85
                            rotationY = PI / 2
                            useStyle(MogStyles.materialStyleName("Motor"))
                        }
                        box(xSize = 3.0, ySize = 1.6, zSize = 1.2, name = "CakeBin") {
                            x = -0.3
                            z = 0.6
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                    }
                }
            }

            // Build Nozzles
            val nozzlesGroup = solidGroup("Nozzles") {
                for (noz in nozzles) {
                    val npth = noz.spec.id
                    solidGroup(npth) {
                        x = noz.connLocal.x
                        y = noz.connLocal.y
                        z = noz.connLocal.z

                        properties.setValue(Name.of("dexpi:class"), "Nozzle".asValue())
                        properties.setValue(Name.of("dt:portType"), noz.spec.port.asValue())
                        properties.setValue(Name.of("dt:DN_mm"), noz.spec.dn.asValue())

                        val rn = dnRadius(noz.spec.dn)
                        val dx = (noz.connLocal.x - noz.baseLocal.x).toDouble()
                        val dy = (noz.connLocal.y - noz.baseLocal.y).toDouble()
                        val dz = (noz.connLocal.z - noz.baseLocal.z).toDouble()
                        val len = sqrt(dx * dx + dy * dy + dz * dz)

                        val stubMat = if (eq.template !in listOf("pump", "fan", "phe", "exchanger", "filterpress", "filter_press")) eq.material else "SS"

                        // Stub
                        cylinder(r = rn, height = len, name = "Stub") {
                            x = -noz.normalLocal.x * len / 2.0
                            y = -noz.normalLocal.y * len / 2.0
                            z = -noz.normalLocal.z * len / 2.0
                            if (abs(noz.normalLocal.x) > 0.5) rotationY = PI / 2
                            if (abs(noz.normalLocal.y) > 0.5) rotationX = -PI / 2
                            useStyle(MogStyles.materialStyleName(stubMat))
                        }

                        // Flange
                        cylinder(r = rn * 1.25 + 0.03, height = 0.06, name = "Flange") {
                            x = -noz.normalLocal.x * 0.03
                            y = -noz.normalLocal.y * 0.03
                            z = -noz.normalLocal.z * 0.03
                            if (abs(noz.normalLocal.x) > 0.5) rotationY = PI / 2
                            if (abs(noz.normalLocal.y) > 0.5) rotationX = -PI / 2
                            useStyle(MogStyles.materialStyleName("Steel"))
                        }
                    }
                }
            }
        }
    }
}
