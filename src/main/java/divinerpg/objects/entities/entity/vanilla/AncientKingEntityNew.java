package divinerpg.objects.entities.entity.vanilla;

import divinerpg.objects.entities.entity.EntityDivineRPGMob;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class AncientKingEntityNew extends EntityDivineRPGMob {
    private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(AncientKingEntityNew.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> NICE = EntityDataManager.createKey(AncientKingEntityNew.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> END_ATTACK = EntityDataManager.createKey(AncientKingEntityNew.class, DataSerializers.BOOLEAN);

    private BlockPos currentFlightTarget = null;
    private EntityLivingBase rt = null;
    //private double attdam = 250.0D;
    private int hurt_timer = 0;
    private int homex = 0;
    private int homez = 0;
    private int stream_count = 0;
    private int stream_count_l = 0;
    private int stream_count_i = 0;
    private int ticker = 0;
    private int player_hit_count = 0;
    private int backoff_timer = 0;
    private int guard_mode = 0;
    private volatile int head_found = 0;
    private int wing_sound = 0;
    private int large_unknown_detected = 0;
    private int isEnd = 0;
    private int endCounter = 0;

    public AncientKingEntityNew(World par1World) {
        super(par1World);
//        if (OreSpawnMain.PlayNicely == 0) {
//            this.setSize(22.0F, 24.0F);
//        } else {
//            this.setSize(5.5F, 6.0F);
//        }

        this.setSize(5.5F, 6.0F);

        //this.getNavigator().setAvoidsWater;

        this.experienceValue = 25000;
        this.isImmuneToFire = true;
        // fireResistance
        //this.field_70174_ab = 5000;
        this.noClip = true;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
    }

    /**
     * Render 12 times further
     *
     * @param distance
     * @return
     */
    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6200000047683716D);
        //Config.getOrRegister(this, SharedMonsterAttributes.ARMOR).setBaseValue(Config.getEntityAttributeValue(this, SharedMonsterAttributes.ARMOR));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        EntityDataManager manager = this.getDataManager();

        manager.register(ANGRY, false);
        manager.register(NICE, true);
        manager.register(END_ATTACK, false);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    public final boolean isAngry() {
        return this.getDataManager().get(ANGRY);
    }

    public final void setIsAngry(boolean isAngry) {
        this.getDataManager().set(ANGRY, isAngry);
    }

    @Override
    protected float getSoundVolume() {
        return 1.35F;
    }

    @Override
    protected float getSoundPitch() {
        return 1.0F;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity par1Entity) {
    }

    public int getMaxHealthInner() {
        // todo
        return 3000;
        //return (int) Config.getEntityAttributeValue(this, SharedMonsterAttributes.MAX_HEALTH);
    }

    public float getMaxAttack() {
        // todo
        return 80;
        //return Config.getEntityAttributeValue(this, SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    @Override
    protected Item getDropItem() {
        return Items.AIR;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        ++this.wing_sound;
        if (this.wing_sound > 30) {
            if (!this.world.isRemote) {
                // todo sound
                //this.world.playSoundAtEntity(this, "orespawn:MothraWings", 1.75F, 0.75F);
            }

            this.wing_sound = 0;
        }

        this.noClip = true;
        this.motionY *= 0.6D;
        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() * 2 / 3)) {
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getMaxAttack() * 2);
        }

        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() / 2)) {
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getMaxAttack() * 4);
        }

        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() / 4)) {
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getMaxAttack() * 8);
        }

        if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() / 8)) {
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getMaxAttack() * 16);
        }

        if (this.world.isRemote) {
            float f = 7.0F;
            if (getIsRapidAttacking() && this.world.rand.nextInt(3) == 1) {
                for (int i = 0; i < 10; ++i) {
                    // todo particle
                    //this.world.spawnParticle("fireworksSpark", this.posX - (double) f * Math.sin(Math.toRadians(this.rotationYaw)), this.posY + 14.0D, this.posZ + (double) f * Math.cos(Math.toRadians(this.rotationYaw)), (this.world.rand.nextGaussian() - this.world.rand.nextGaussian()) / 4.0D + this.motionX * 6.0D, (this.world.rand.nextGaussian() - this.world.rand.nextGaussian()) / 4.0D, (this.world.rand.nextGaussian() - this.world.rand.nextGaussian()) / 4.0D + this.motionZ * 6.0D);
                }
            }
        }

    }

    public boolean attackEntityAsMob(Entity par1Entity) {
        EntityLivingBase var21;
        if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
//            float s = par1Entity.field_70131_O * par1Entity.field_70130_N;
//            if (s > 30.0F && !MyUtils.isRoyalty(par1Entity) && !(par1Entity instanceof Godzilla) && !(par1Entity instanceof GodzillaHead) && !(par1Entity instanceof PitchBlack) && !(par1Entity instanceof Kraken)) {
//                var21 = (EntityLivingBase)par1Entity;
//                var21.setHealth(var21.func_110143_aJ() / 2.0F);
//                var21.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.attdam * 10.0F);
//                this.large_unknown_detected = 1;
//            }
        }

//        if (par1Entity != null && par1Entity instanceof EntityDragon) {
//            EntityDragon dr = (EntityDragon)par1Entity;
//            var21 = null;
//            DamageSource var21 = DamageSource.func_94539_a((Explosion)null);
//            var21.func_94540_d();
//            if (this.world.rand.nextInt(6) == 1) {
//                dr.func_70965_a(dr.field_70986_h, var21, (float)this.attdam);
//            } else {
//                dr.func_70965_a(dr.field_70987_i, var21, (float)this.attdam);
//            }
//        }

        boolean var4 = par1Entity.attackEntityFrom(DamageSource.causeMobDamage(this), (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (var4) {
            double ks = 3.3D;
            double inair = 0.25D;
            float f3 = (float) Math.atan2(par1Entity.posZ - this.posZ, par1Entity.posX - this.posX);
            inair += this.world.rand.nextFloat() * 0.25F;
            if (par1Entity.isDead || par1Entity instanceof EntityPlayer) {
                inair *= 1.5D;
            }

            par1Entity.addVelocity(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }

        return var4;
    }

//    public boolean canSeeTarget(double pX, double pY, double pZ) {
//        return this.world.rayTraceBlocks(Vec3d.createVectorHelper(this.posX, this.posY + 8.75D, this.posZ), Vec3.func_72443_a(pX, pY, pZ), false) == null;
//    }

    private boolean tooFarFromHome() {
        float d1 = (float) (this.posX - (double) this.homex);
        float d2 = (float) (this.posZ - (double) this.homez);
        d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
        return d1 > 120.0F;
    }

    private void msgToPlayers(String s) {
        List var5 = this.world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().expand(80.0D, 64.0D, 80.0D));
        Iterator var2 = var5.iterator();
        Entity var3;
        EntityPlayer var4;

        while (var2.hasNext()) {
            var3 = (Entity) var2.next();
            var4 = (EntityPlayer) var3;
            var4.sendMessage(new TextComponentString(s));
        }

    }


    private EntityPlayer findNearestPlayer() {
        List var5 = this.world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().expand(80.0D, 64.0D, 80.0D));
        Iterator var2 = var5.iterator();
        Entity var3;
        EntityPlayer var4 = null;

        while (var2.hasNext()) {
            var3 = (Entity) var2.next();
            if (var3 instanceof EntityPlayer) {
                var4 = (EntityPlayer) var3;
            }

            if (var4 != null) {
                break;
            }
        }

        return var4;
    }

    @Override
    protected void updateAITasks() {
        int xdir = 1;
        int zdir = 1;
        int attrand = 5;
        int updown = 0;
        int which = 0;
        EntityLivingBase e;
        EntityLivingBase f;
        double rr;
        double rhdir;
        double rdd;
        double pi = 3.1415926545D;
        double var1;
        double var3;
        double var5;
        float var7;
        float var8;
        EntityPlayer p;
        if (!this.isDead) {
            super.updateAITasks();
            double dx;
            double dz;
            if (!this.getIsRapidAttacking()) {
                ++this.endCounter;
                this.noClip = true;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
                this.hurt_timer = 10;
                if (!this.isDead) {
                    p = this.findNearestPlayer();
                    if (p != null) {
                        this.faceEntity(p, 10.0F, 10.0F);
                        p.motionX = 0.0D;
                        p.motionY = 0.0D;
                        p.motionZ = 0.0D;
                        dx = this.posX - p.posX;
                        dz = this.posZ - p.posZ;
                        float f2 = (float) (Math.atan2(dz, dx) * 180.0D / 3.141592653589793D) - 90.0F;
                        p.rotationYaw = f2;
                        p.setHealth(1.0F);
                    }

                    if (this.endCounter == 10) {
                        this.msgToPlayers("The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
                    } else if (this.endCounter == 80) {
                        this.msgToPlayers("The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
                    } else if (this.endCounter == 160) {
                        this.msgToPlayers("The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
                    } else if (this.endCounter == 240) {
                        this.msgToPlayers("The King: I used you to learn your ways, and I have reached my conclusion on your species.");
                    } else if (this.endCounter == 300) {
                        this.msgToPlayers("The King: You have 10 seconds to run...");
                    } else if (this.endCounter == 320) {
                        this.msgToPlayers("9.");
                    } else if (this.endCounter == 340) {
                        this.msgToPlayers("8.");
                    } else if (this.endCounter == 360) {
                        this.msgToPlayers("7.");
                    } else if (this.endCounter == 380) {
                        this.msgToPlayers("6.");
                    } else if (this.endCounter == 400) {
                        this.msgToPlayers("5.");
                    } else if (this.endCounter == 420) {
                        this.msgToPlayers("4.");
                    } else if (this.endCounter == 440) {
                        this.msgToPlayers("3.");
                    } else if (this.endCounter == 460) {
                        this.msgToPlayers("2.");
                    } else if (this.endCounter == 480) {
                        this.msgToPlayers("1.");
                    } else if (this.endCounter == 500) {
                        this.msgToPlayers("The King: Prepare to die!");
                        this.setIsRapidAttacking(true);
                    }
                }
            } else {
                if (this.getIsRapidAttacking()) {
                    this.hurt_timer = 10;
                    this.player_hit_count = 0;
                    this.stream_count = 10;
                    this.stream_count_l = 10;
                    this.stream_count_i = 10;
                    attrand = 3;
                    this.guard_mode = 0;
                    this.large_unknown_detected = 1;
                    if (this.backoff_timer > 0) {
                        --this.backoff_timer;
                    }
                }

                if (this.hurt_timer > 0) {
                    --this.hurt_timer;
                }

                if (this.homex == 0 && this.homez == 0 || this.guard_mode == 0) {
                    this.homex = (int) this.posX;
                    this.homez = (int) this.posZ;
                }

                ++this.ticker;
                if (this.ticker > 30000) {
                    this.ticker = 0;
                }

                if (this.ticker % 80 == 0) {
                    this.stream_count = 10;
                }

                if (this.ticker % 90 == 0) {
                    this.stream_count_l = 5;
                }

                if (this.ticker % 70 == 0) {
                    this.stream_count_i = 8;
                }

                if (this.backoff_timer > 0) {
                    --this.backoff_timer;
                }

                if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() / 2)) {
                    attrand = 3;
                }

                this.noClip = true;
                if (this.currentFlightTarget == null) {
                    this.currentFlightTarget = getPosition();
                }

                int i;
                int j;
                int k;
                int dist;
                IBlockState bid;
//                int xdir;
//                int zdir;
                if (!this.tooFarFromHome() && this.world.rand.nextInt(200) != 0 && this.currentFlightTarget.distanceSq((int) this.posX, (int) this.posY, (int) this.posZ) >= 9.1F) {
                    if (this.world.rand.nextInt(attrand) == 0) {
                        e = this.rt;
                        if (getDataManager().get(NICE)) {
                            e = null;
                        }

//                        if (e != null && (e instanceof AncientKingEntityNew || e instanceof KingHead)) {
//                            this.rt = null;
//                            e = null;
//                        }

                        if (e != null) {
                            float d1 = (float) (e.posX - (double) this.homex);
                            float d2 = (float) (e.posZ - (double) this.homez);
                            d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
                            if (e.isDead || this.world.rand.nextInt(250) == 1 || d1 > 128.0F && this.guard_mode == 1) {
                                e = null;
                                this.rt = null;
                            }

                            if (e != null && !this.MyCanSee(e)) {
                                e = null;
                            }
                        }

                        f = this.findSomethingToAttack();
                        if (this.head_found == 0) {
                            // EntityLiving var10000 = (EntityLiving) spawnCreature(this.world, "KingHead", this.posX, this.posY + 20.0D, this.posZ);
                        }

                        if (e == null) {
                            e = f;
                        }

                        if (e != null) {
                            this.setIsAngry(true);
                            if (this.backoff_timer == 0) {
                                dist = (int) (e.posY + (e.height / 2.0F) + 1.0D);
                                if (dist > 230) {
                                    dist = 230;
                                }

                                this.currentFlightTarget = new BlockPos(e.posX, dist, (int) e.posZ);

                                if (this.world.rand.nextInt(70) == 1) {
                                    this.backoff_timer = 80 + this.world.rand.nextInt(80);
                                }
                            } else if (this.currentFlightTarget.distanceSq((int) this.posX, (int) this.posY, (int) this.posZ) < 9.1F) {
                                zdir = this.world.rand.nextInt(20) + 30;
                                xdir = this.world.rand.nextInt(20) + 30;
                                if (this.world.rand.nextInt(2) == 0) {
                                    zdir = -zdir;
                                }

                                if (this.world.rand.nextInt(2) == 0) {
                                    xdir = -xdir;
                                }

                                dist = 0;
                                i = -5;

                                while (true) {
                                    if (i > 5) {
                                        dist = dist / 9 + 2;
                                        if ((int) (this.posY + (double) dist) > 230) {
                                            dist = 230 - (int) this.posY;
                                        }

                                        this.currentFlightTarget = new BlockPos((int) e.posX + xdir, (int) (this.posY + (double) dist), (int) e.posZ + zdir);
                                        break;
                                    }

                                    for (j = -5; j <= 5; j += 5) {
                                        bid = this.world.getBlockState(new BlockPos((int) e.posX + j, (int) this.posY, (int) e.posZ + i));
                                        if (bid.getMaterial() == Material.AIR) {
                                            for (k = 1; k < 20; ++k) {
                                                bid = this.world.getBlockState(new BlockPos((int) e.posX + j, (int) this.posY + k, (int) e.posZ + i));
                                                ++dist;
                                                if (bid.getMaterial() == Material.AIR) {
                                                    break;
                                                }
                                            }
                                        } else {
                                            for (k = 1; k < 20; ++k) {
                                                bid = this.world.getBlockState(new BlockPos((int) e.posX + j, (int) this.posY - k, (int) e.posZ + i));
                                                --dist;
                                                if (bid.getMaterial() != Material.AIR) {
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    i += 5;
                                }
                            }

                            if (this.getDistanceSq(e) < 900.0D) {
                                if (this.world.rand.nextInt(2) == 1) {
                                    this.doJumpDamage(this.posX, this.posY, this.posZ, 15.0D, getMaxAttack() / 4, 0);
                                }

                                this.attackEntityAsMob(e);
                            }

                            dx = this.posX + 20.0D * Math.sin(Math.toRadians((double) this.rotationYawHead));
                            dz = this.posZ - 20.0D * Math.cos(Math.toRadians((double) this.rotationYawHead));
                            if (this.world.rand.nextInt(3) == 1) {
                                this.doJumpDamage(dx, this.posY + 10.0D, dz, 15.0D, getMaxAttack() / 2, 1);
                            }

                            if (this.getHorizontalDistanceSqToEntity(e) > 900.0D) {
                                which = this.world.rand.nextInt(3);
                                if (which == 0) {
                                    if (this.stream_count > 0) {
                                        this.setIsAngry(true);
                                        rr = Math.atan2(e.posZ - this.posZ, e.posX - this.posX);
                                        rhdir = Math.toRadians((double) ((this.rotationYawHead + 90.0F) % 360.0F));
                                        rdd = Math.abs(rr - rhdir) % (pi * 2.0D);
                                        if (rdd > pi) {
                                            rdd -= pi * 2.0D;
                                        }

                                        rdd = Math.abs(rdd);
                                        if (rdd < 0.5D) {
                                            this.firecanon(e);
                                        }
                                    }
                                } else if (which == 1) {
                                    if (this.stream_count_l > 0) {
                                        this.setIsAngry(true);
                                        rr = Math.atan2(e.posZ - this.posZ, e.posX - this.posX);
                                        rhdir = Math.toRadians((double) ((this.rotationYawHead + 90.0F) % 360.0F));
                                        rdd = Math.abs(rr - rhdir) % (pi * 2.0D);
                                        if (rdd > pi) {
                                            rdd -= pi * 2.0D;
                                        }

                                        rdd = Math.abs(rdd);
                                        if (rdd < 0.5D) {
                                            this.fireCannons(e);
                                        }
                                    }
                                } else if (this.stream_count_i > 0) {
                                    this.setIsAngry(true);
                                    rr = Math.atan2(e.posZ - this.posZ, e.posX - this.posX);
                                    rhdir = Math.toRadians((double) ((this.rotationYawHead + 90.0F) % 360.0F));
                                    rdd = Math.abs(rr - rhdir) % (pi * 2.0D);
                                    if (rdd > pi) {
                                        rdd -= pi * 2.0D;
                                    }

                                    rdd = Math.abs(rdd);
                                    if (rdd < 0.5D) {
                                        this.firecanoni(e);
                                    }
                                }
                            }
                        } else {
                            this.setIsAngry(false);
                            this.stream_count = 10;
                            this.stream_count_l = 5;
                            this.stream_count_i = 8;
                        }
                    }
                } else {
                    zdir = this.world.rand.nextInt(120);
                    xdir = this.world.rand.nextInt(120);
                    if (this.world.rand.nextInt(2) == 0) {
                        zdir = -zdir;
                    }

                    if (this.world.rand.nextInt(2) == 0) {
                        xdir = -xdir;
                    }

                    dist = 0;

                    for (i = -5; i <= 5; i += 5) {
                        for (j = -5; j <= 5; j += 5) {
                            bid = this.world.getBlockState(new BlockPos(this.homex + j, (int) this.posY, this.homez + i));
                            if (bid.getMaterial() != Material.AIR) {
                                for (k = 1; k < 20; ++k) {
                                    bid = this.world.getBlockState(new BlockPos(this.homex + j, (int) this.posY + k, this.homez + i));
                                    ++dist;
                                    if (bid.getMaterial() == Material.AIR) {
                                        break;
                                    }
                                }
                            } else {
                                for (k = 1; k < 20; ++k) {
                                    bid = this.world.getBlockState(new BlockPos(this.homex + j, (int) this.posY - k, this.homez + i));
                                    --dist;
                                    if (bid.getMaterial() != Material.AIR) {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    dist = dist / 9 + 2;
                    if ((int) (this.posY + (double) dist) > 230) {
                        dist = 230 - (int) this.posY;
                    }

                    this.currentFlightTarget = new BlockPos(this.homex + xdir, (int) (this.posY + (double) dist), this.homez + zdir);
                }

                if (this.isAngry() && this.getIsRapidAttacking()) {
                    dx = 10.0D;
                    dz = 14.0D;
//                    Entity ppwr = spawnCreature(this.world, "PurplePower", this.posX - dx * Math.sin(Math.toRadians(this.rotationYaw)), this.posY + dz, this.posZ + dx * Math.cos(Math.toRadians(this.rotationYaw)));
//                    if (ppwr != null) {
//                        PurplePower pwr = (PurplePower) ppwr;
//                        pwr.motionX = this.motionX * 3.0D;
//                        pwr.motionZ = this.motionZ * 3.0D;
//                        pwr.setPurpleType(10);
//                    }
                }

                var1 = (double) this.currentFlightTarget.getX() + 0.5D - this.posX;
                var3 = (double) this.currentFlightTarget.getY() + 0.1D - this.posY;
                var5 = (double) this.currentFlightTarget.getZ() + 0.5D - this.posZ;
                this.motionX += (Math.signum(var1) * 0.7D - this.motionX) * 0.35D;
                this.motionY += (Math.signum(var3) * 0.69999D - this.motionY) * 0.3D;
                this.motionZ += (Math.signum(var5) * 0.7D - this.motionZ) * 0.35D;
                var7 = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / 3.141592653589793D) - 90.0F;
                var8 = MathHelper.wrapDegrees(var7 - this.rotationYaw);
                this.moveForward = 1.0F;
                this.rotationYaw += var8 / 8.0F;
                if (this.world.rand.nextInt(30) == 1 && this.getHealth() < (float) this.getMaxHealthInner()) {
                    this.heal(5.0F);
                    if (this.large_unknown_detected != 0) {
                        this.heal(200.0F);
                    }
                }

                if (this.player_hit_count < 10 && this.getHealth() < 2000.0F) {
                    this.heal(2000.0F - this.getHealth());
                }

            }
        }
    }

    private double getHorizontalDistanceSqToEntity(Entity e) {
        double d1 = e.posZ - this.posZ;
        double d2 = e.posX - this.posX;
        return d1 * d1 + d2 * d2;
    }

    private void firecanon(EntityLivingBase e) {
        double yoff = 14.0D;
        double xzoff = 32.0D;
        double cx = this.posX - xzoff * Math.sin(Math.toRadians(this.rotationYaw));
        double cz = this.posZ + xzoff * Math.cos(Math.toRadians(this.rotationYaw));
        if (this.stream_count > 0) {
//            BetterFireball bf;
//            bf = new BetterFireball(this.world, this, e.posX - cx, e.posY + (double) (e.height / 2.0F) - (this.posY + yoff), e.posZ - cz);
//            bf.func_70012_b(cx, this.posY + yoff, cz, this.rotationYaw, 0.0F);
//            bf.func_70107_b(cx, this.posY + yoff, cz);
//            bf.setReallyBig();
//            this.world.playSoundAtEntity(this, "random.fuse", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));
//            this.world.spawnEntity(bf);
//
//            for (int i = 0; i < 6; ++i) {
//                float r1 = 5.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                float r2 = 3.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                float r3 = 5.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                bf = new BetterFireball(this.world, this, e.posX - cx + (double) r1, e.posY + (double) (e.height / 2.0F) - (this.posY + yoff) + (double) r2, e.posZ - cz + (double) r3);
//                bf.func_70012_b(cx, this.posY + yoff, cz, this.rotationYaw, 0.0F);
//                bf.func_70107_b(cx, this.posY + yoff, cz);
//                bf.setBig();
//                if (this.world.rand.nextInt(2) == 1) {
//                    bf.setSmall();
//                }
//
//                this.world.playSoundAtEntity(this, "random.bow", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));
//                this.world.spawnEntity(bf);
//            }

            --this.stream_count;
        }

    }

    private void fireCannons(EntityLivingBase e) {
        double yoff = 14.0D;
        double xzoff = 32.0D;
        double var3;
        double var5;
        double var7;
        float var9;
        double cx = this.posX - xzoff * Math.sin(Math.toRadians(this.rotationYaw));
        double cz = this.posZ + xzoff * Math.cos(Math.toRadians(this.rotationYaw));
        if (this.stream_count_l > 0) {
            //this.world.playSoundAtEntity(this, "random.bow", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));

//            for (int i = 0; i < 3; ++i) {
//                float var10000 = 5.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                var10000 = 3.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                var10000 = 5.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                ThunderBolt lb = new ThunderBolt(this.world, cx, this.posY + yoff, cz);
//                lb.func_70012_b(cx, this.posY + yoff, cz, 0.0F, 0.0F);
//                var3 = e.posX - lb.posX;
//                var5 = e.posY + 0.25D - lb.posY;
//                var7 = e.posZ - lb.posZ;
//                var9 = MathHelper.sqrt(var3 * var3 + var7 * var7) * 0.2F;
//                lb.func_70186_c(var3, var5 + (double) var9, var7, 1.4F, 4.0F);
//                lb.motionX *= 3.0D;
//                lb.motionY *= 3.0D;
//                lb.motionZ *= 3.0D;
//                this.world.spawnEntity(lb);
//            }

            --this.stream_count_l;
        }

    }

    private void firecanoni(EntityLivingBase e) {
        double yoff = 14.0D;
        double xzoff = 32.0D;
        double var3;
        double var5;
        double var7;
        float var9;
        double cx = this.posX - xzoff * Math.sin(Math.toRadians(this.rotationYaw));
        double cz = this.posZ + xzoff * Math.cos(Math.toRadians(this.rotationYaw));
        if (this.stream_count_i > 0) {
            //this.world.playSoundAtEntity(this, "random.bow", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));

//            for (int i = 0; i < 5; ++i) {
//                float var10000 = 5.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                var10000 = 3.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                var10000 = 5.0F * (this.world.rand.nextFloat() - this.world.rand.nextFloat());
//                IceBall lb = new IceBall(this.world, cx, this.posY + yoff, cz);
//                lb.setIceMaker(1);
//                lb.func_70012_b(cx, this.posY + yoff, cz, 0.0F, 0.0F);
//                var3 = e.posX - lb.posX;
//                var5 = e.posY + 0.25D - lb.posY;
//                var7 = e.posZ - lb.posZ;
//                var9 = MathHelper.sqrt(var3 * var3 + var7 * var7) * 0.2F;
//                lb.set(var3, var5 + (double) var9, var7, 1.4F, 4.0F);
//                lb.motionX *= 3.0D;
//                lb.motionY *= 3.0D;
//                lb.motionZ *= 3.0D;
//                this.world.spawnEntity(lb);
//            }

            --this.stream_count_i;
        }

    }

    @Override
    protected boolean canTriggerWalking() {
        return true;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {

    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {

    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return false;
    }

    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        float dm = par2;
        if (this.hurt_timer > 0) {
            return false;
        } else {
            if (par2 > 750.0F) {
                dm = 750.0F;
            }

            if (par1DamageSource.getDamageType().equals(DamageSource.IN_WALL.damageType)) {
                return false;
            } else {
                Entity e = par1DamageSource.getTrueSource();
                if (e != null && e instanceof EntityLivingBase) {
                    EntityLivingBase enl = (EntityLivingBase) e;
                    float s = enl.height * enl.width;
                    if (s > 30.0F) {
                        dm /= 10.0F;
                        this.hurt_timer = 50;
                        this.large_unknown_detected = 1;
                    }

                    if (e instanceof EntityMob && s < 3.0F) {
                        e.setDead();
                        return false;
                    }
                }

                if (!par1DamageSource.getDamageType().equals("cactus")) {
                    this.hurt_timer = 20;
                    ret = super.attackEntityFrom(par1DamageSource, dm);
                    if (e != null && e instanceof EntityPlayer) {
                        ++this.player_hit_count;
                    }

                    if (e != null && e instanceof EntityLivingBase && this.currentFlightTarget != null) {
                        this.rt = (EntityLivingBase) e;
                        int dist = (int) e.posY;
                        if (dist > 230) {
                            dist = 230;
                        }

                        this.currentFlightTarget = new BlockPos((int) e.posX, dist, (int) e.posZ);
                    }
                }

                return ret;
            }
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return true;
    }

    public int getDefaultArmorValue() {
        return 21;
    }

    @Override
    public int getTotalArmorValue() {
        if (this.large_unknown_detected != 0) {
            return 25;
        } else if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() * 2 / 3)) {
            return getDefaultArmorValue() + 1;
        } else if (this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() / 2)) {
            return getDefaultArmorValue() + 2;
        } else {
            return this.player_hit_count < 10 && this.getHealth() < (float) (this.getMaxHealthInner() / 4) ? getDefaultArmorValue() + 3 : getDefaultArmorValue();
        }
    }

    public void onStruckByLightning(EntityLightningBolt par1EntityLightningBolt) {
    }

    public boolean MyCanSee(EntityLivingBase e) {
        double xzoff = 22.0D;
        int nblks = 20;
        double cx = this.posX - xzoff * Math.sin(Math.toRadians(this.rotationYaw));
        double cz = this.posZ + xzoff * Math.cos(Math.toRadians(this.rotationYaw));
        float startx = (float) cx;
        float starty = (float) (this.posY + (double) (this.height * 7.0F / 8.0F));
        float startz = (float) cz;
        float dx = (float) ((e.posX - (double) startx) / 20.0D);
        float dy = (float) ((e.posY + (double) (e.height / 2.0F) - (double) starty) / 20.0D);
        float dz = (float) ((e.posZ - (double) startz) / 20.0D);
        if ((double) Math.abs(dx) > 1.0D) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) ((float) nblks * Math.abs(dx));
            if (dx > 1.0F) {
                dx = 1.0F;
            }

            if (dx < -1.0F) {
                dx = -1.0F;
            }
        }

        if ((double) Math.abs(dy) > 1.0D) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) ((float) nblks * Math.abs(dy));
            if (dy > 1.0F) {
                dy = 1.0F;
            }

            if (dy < -1.0F) {
                dy = -1.0F;
            }
        }

        if ((double) Math.abs(dz) > 1.0D) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) ((float) nblks * Math.abs(dz));
            if (dz > 1.0F) {
                dz = 1.0F;
            }

            if (dz < -1.0F) {
                dz = -1.0F;
            }
        }

        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            IBlockState bid = this.world.getBlockState(new BlockPos((int) startx, (int) starty, (int) startz));
            if (bid.getMaterial() != Material.VINE
                    && bid.getMaterial() != Material.LEAVES
                    && bid.getMaterial() != Material.AIR
                    && bid != Blocks.WATER) {
                return false;
            }
        }

        return true;
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        } else if (par1EntityLiving == this) {
            return false;
        } else if (!par1EntityLiving.isEntityAlive()) {
            return false;
        } /*else if (par1EntityLiving instanceof KingHead) {
            this.head_found = 1;
            return false;
        } else if (MyUtils.isRoyalty(par1EntityLiving)) {
            return false;
        }*/ else {
            float d1 = (float) (par1EntityLiving.posX - (double) this.homex);
            float d2 = (float) (par1EntityLiving.posZ - (double) this.homez);
            d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
            if (d1 > 144.0F) {
                return false;
            } else {
                /*MyUtils var10000 = OreSpawnMain.OreSpawnUtils;
                if (MyUtils.isIgnoreable(par1EntityLiving)) {
                    return false;
                } else*/
                {
                    EntityPlayer p;
                    if (this.getIsRapidAttacking()) {
                        if (par1EntityLiving instanceof EntityPlayer) {
                            p = (EntityPlayer) par1EntityLiving;
                            if (p.isCreative()) {
                                return false;
                            }

                            return true;
                        }

//                        if (par1EntityLiving instanceof Girlfriend) {
//                            return true;
//                        }
//
//                        if (par1EntityLiving instanceof Boyfriend) {
//                            return true;
//                        }
//
//                        if (par1EntityLiving instanceof EntityVillager) {
//                            return true;
//                        }
                    }

                    if (!this.MyCanSee(par1EntityLiving)) {
                        return false;
                    } else if (par1EntityLiving instanceof EntityPlayer) {
                        p = (EntityPlayer) par1EntityLiving;
                        return !p.isCreative();
                    } else if (par1EntityLiving instanceof EntityHorse) {
                        return true;
                    } else if (par1EntityLiving instanceof EntityMob) {
                        return true;
                    } else if (par1EntityLiving instanceof EntityDragon) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    private EntityLivingBase findSomethingToAttack() {
        if (getDataManager().get(NICE)) {
            this.head_found = 1;
            return null;
        } else {
            List var5;
            Iterator var2;
            Entity var3;
            EntityLivingBase var4;
            EntityLivingBase ret;
            if (this.getIsRapidAttacking()) {
                var5 = this.world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().expand(80.0D, 64.0D, 80.0D));
//                Collections.sort(var5, this.TargetSorter);
                var2 = var5.iterator();
                this.head_found = 1;

                while (var2.hasNext()) {
                    var3 = (Entity) var2.next();
                    var4 = (EntityLivingBase) var3;
                    if (this.isSuitableTarget(var4, false)) {
                        return var4;
                    }
                }
            }

            var5 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().expand(80.0D, 64.0D, 80.0D));
//            Collections.sort(var5, this.TargetSorter);
            var2 = var5.iterator();
            ret = null;
            this.head_found = 0;

            while (var2.hasNext()) {
                var3 = (Entity) var2.next();
                var4 = (EntityLivingBase) var3;
                if (this.isSuitableTarget(var4, false) && ret == null) {
                    ret = var4;
                }

                if (ret != null && this.head_found != 0) {
                    break;
                }
            }

            return ret;
        }
    }

    public void setGuardMode(int i) {
        this.guard_mode = i;
    }

    public void setFree() {
        this.setIsRapidAttacking(false);
    }

    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("KingHomeX", this.homex);
        nbt.setInteger("KingHomeZ", this.homez);
        nbt.setInteger("GuardMode", this.guard_mode);
        nbt.setInteger("PlayerHits", this.player_hit_count);
        nbt.setBoolean("IsEnd", this.getIsRapidAttacking());
        nbt.setInteger("EndCounter", this.endCounter);
    }

    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.homex = par1NBTTagCompound.getInteger("KingHomeX");
        this.homez = par1NBTTagCompound.getInteger("KingHomeZ");
        this.guard_mode = par1NBTTagCompound.getInteger("GuardMode");
        this.player_hit_count = par1NBTTagCompound.getInteger("PlayerHits");
        this.setIsRapidAttacking(par1NBTTagCompound.getBoolean("IsEnd"));
        this.endCounter = par1NBTTagCompound.getInteger("EndCounter");
    }

    public static Entity spawnCreature(World par0World, ResourceLocation par1, double par2, double par4, double par6) {
        Entity var8;
        var8 = EntityList.createEntityByIDFromName(par1, par0World);
        if (var8 != null) {
            var8.setLocationAndAngles(par2, par4, par6, par0World.rand.nextFloat() * 360.0F, 0.0F);
            par0World.spawnEntity(var8);
        }

        return var8;
    }

    private EntityLivingBase doJumpDamage(double X, double Y, double Z, double dist, double damage, int knock) {
        AxisAlignedBB bb = new AxisAlignedBB(X - dist, Y - 10.0D, Z - dist, X + dist, Y + 10.0D, Z + dist);
        List var5 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
//        Collections.sort(var5, this.TargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3;
        EntityLivingBase var4;

        while (var2.hasNext()) {
            var3 = (Entity) var2.next();
            var4 = (EntityLivingBase) var3;
            if (var4 != null && var4 != this && var4.isEntityAlive()/* && !MyUtils.isRoyalty(var4) && !(var4 instanceof Ghost) && !(var4 instanceof GhostSkelly)*/) {
                DamageSource var21;
                var21 = DamageSource.causeExplosionDamage((Explosion) null);
                var21.setExplosion();
                var4.attackEntityFrom(var21, (float) damage / 2.0F);
                var4.attackEntityFrom(DamageSource.FALL, (float) damage / 2.0F);
                //this.world.playSoundAtEntity(var4, "random.explode", 0.65F, 1.0F + (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.5F);
                if (knock != 0) {
                    double ks = 2.75D;
                    double inair = 0.65D;
                    float f3 = (float) Math.atan2(var4.posZ - this.posZ, var4.posX - this.posX);
                    var4.addVelocity(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
                }
            }
        }

        return null;
    }

    public boolean getIsRapidAttacking() {
        return getDataManager().get(END_ATTACK);
    }

    public void setIsRapidAttacking(boolean isEnd) {
        getDataManager().set(END_ATTACK, isEnd);
    }
}
