package com.nes.core;

import com.nes.goncurrent.Chan;
import com.nes.goncurrent.Select;

import static com.nes.util.Constant.frameCounterRate;
import static com.nes.util.Constant.sampleRate;

/**
 * Created by RoyChan on 2018/3/8.
 */


public class APU {

    private int[] lengthTable = new int[]{
        10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
                12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30,
    };

    private byte[][] dutyTable = new byte[][]{
        {0, 1, 0, 0, 0, 0, 0, 0},
        {0, 1, 1, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 1, 0, 0, 0},
        {1, 0, 0, 1, 1, 1, 1, 1},
    };

    private byte[] triangleTable = new byte[]{
        15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    };

    private int[] noiseTable = new int[]{
        4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068,
    };

    private int[] dmcTable = new int[]{
        214, 190, 170, 160, 143, 127, 113, 107, 95, 80, 71, 64, 53, 42, 36, 27,
    };

    private float[] pulseTable = new float[31];
    private float[] tndTable = new float[203];

    private void init(){
        for (int i = 0; i < 31; i++) {
            pulseTable[i] = new Float(95.52 / (8128.0/i + 100));
        }

        for (int i = 0; i < 203; i++) {
            tndTable[i] =  new Float(163.67 / (24329.0/i + 100));
        }
    }


    Console console;
    Chan<Float> channel;
    Pulse pulse1;
    Pulse pulse2;
    Triangle triangle;
    Noise noise;
    DMC	dmc;
    int cycle;
    byte framePeriod;
    byte frameValue;
    boolean frameIRQ;

    public APU(Console console){
        this.console = console;
        this.noise.shiftRegister = 1;
        this.pulse1.channel = 1;
        this.pulse2.channel = 2;
        this.dmc.cpu = console.CPU;
    }

    public void Step() {
        int cycle1 = this.cycle;
        this.cycle++;
        int cycle2 = this.cycle;
        this.stepTimer();
        int f1 = (int) (Float.valueOf(cycle1) / frameCounterRate);
        int f2 = (int) (Float.valueOf(cycle2) / frameCounterRate);
        if(f1 != f2){
            this.stepFrameCounter();
        }
        int s1 = (int) (Float.valueOf(cycle1) / sampleRate);
        int s2 = (int) (Float.valueOf(cycle2) / sampleRate);
        if(s1 != s2){
            this.sendSample();
        }
    }

    public void sendSample() {
        Select select = new Select();
        select.receive(this.channel);
        int index = select.select(); // wait until one of the channels is ready.
        if (index == 1) {
        }else {
        }
    }

    public float output(){
        byte p1 = this.pulse1.output();
        byte p2 = this.pulse2.output();
        byte t = this.triangle.output();
        byte n = this.noise.output();
        byte d = this.dmc.output();
        float pulseOut = pulseTable[p1+p2];
        float tndOut = tndTable[3*t+2*n+d];
        return pulseOut + tndOut;
    }

    // mode 0:    mode 1:       function
// ---------  -----------  -----------------------------
//  - - - f    - - - - -    IRQ (if bit 6 is clear)
//  - l - l    l - l - -    Length counter and sweep
//  e e e e    e e e e -    Envelope and linear counter
    public void stepFrameCounter() {
        switch(this.framePeriod){
            case 4:
                this.frameValue = (byte) ((this.frameValue + 1) % 4);
                switch(this.frameValue){
                    case 0:
                    case 2:
                        this.stepEnvelope();
                    case 1:
                        this.stepEnvelope();
                        this.stepSweep();
                        this.stepLength();
                    case 3:
                        this.stepEnvelope();
                        this.stepSweep();
                        this.stepLength();
                        this.fireIRQ();
            }
            case 5:
                this.frameValue = (byte) ((this.frameValue + 1) % 5);
                switch(this.frameValue){
                    case 1:
                    case 3:
                        this.stepEnvelope();
                    case 0:
                    case 2:
                        this.stepEnvelope();
                        this.stepSweep();
                        this.stepLength();
            }
        }
    }

    public void stepTimer() {
        if(this.cycle%2 == 0){
            this.pulse1.stepTimer();
            this.pulse2.stepTimer();
            this.noise.stepTimer();
            this.dmc.stepTimer();
        }
        this.triangle.stepTimer();
    }

    public void stepEnvelope(){
        this.pulse1.stepEnvelope();
        this.pulse2.stepEnvelope();
        this.triangle.stepCounter();
        this.noise.stepEnvelope();
    }

    public void stepSweep() {
        this.pulse1.stepSweep();
        this.pulse2.stepSweep();
    }

    public void stepLength() {
        this.pulse1.stepLength();
        this.pulse2.stepLength();
        this.triangle.stepLength();
        this.noise.stepLength();
    }

    public void fireIRQ() {
        if(this.frameIRQ){
            this.console.CPU.triggerIRQ();
        }
    }

    public byte readRegister(int address){
        switch (address){
            case 0x4015:
                return this.readStatus();
            // default:
            // 	log.Fatalf("unhandled apu register read at address: 0x%04X", address)
        }
        return 0;
    }

    public void writeRegister(int address, byte value) {
        switch (address){
            case 0x4000:
                this.pulse1.writeControl(value);
            case 0x4001:
                this.pulse1.writeSweep(value);
            case 0x4002:
                this.pulse1.writeTimerLow(value);
            case 0x4003:
                this.pulse1.writeTimerHigh(value);
            case 0x4004:
                this.pulse2.writeControl(value);
            case 0x4005:
                this.pulse2.writeSweep(value);
            case 0x4006:
                this.pulse2.writeTimerLow(value);
            case 0x4007:
                this.pulse2.writeTimerHigh(value);
            case 0x4008:
                this.triangle.writeControl(value);
            case 0x4009:
            case 0x4010:
                this.dmc.writeControl(value);
            case 0x4011:
                this.dmc.writeValue(value);
            case 0x4012:
                this.dmc.writeAddress(value);
            case 0x4013:
                this.dmc.writeLength(value);
            case 0x400A:
                this.triangle.writeTimerLow(value);
            case 0x400B:
                this.triangle.writeTimerHigh(value);
            case 0x400C:
                this.noise.writeControl(value);
            case 0x400D:
            case 0x400E:
                this.noise.writePeriod(value);
            case 0x400F:
                this.noise.writeLength(value);
            case 0x4015:
                this.writeControl(value);
            case 0x4017:
                this.writeFrameCounter(value);
                // default:
                // 	log.Fatalf("unhandled apu register write at address: 0x%04X", address)
        }
    }

    public byte readStatus(){
        byte result = 0;
        if(this.pulse1.lengthValue > 0){
            result |= 1;
        }
        if(this.pulse2.lengthValue > 0){
            result |= 2;
        }
        if(this.triangle.lengthValue > 0){
            result |= 4;
        }
        if(this.noise.lengthValue > 0){
            result |= 8;
        }
        if(this.dmc.currentLength > 0){
            result |= 16;
        }
        return result;
    }

    public void writeControl(byte value) {
        this.pulse1.enabled = (value&1) == 1;
        this.pulse2.enabled = (value&2) == 2;
        this.triangle.enabled = (value&4) == 4;
        this.noise.enabled = (value&8) == 8;
        this.dmc.enabled = (value&16) == 16;
        if(!this.pulse1.enabled){
            this.pulse1.lengthValue = 0;
        }
        if(!this.pulse2.enabled){
            this.pulse2.lengthValue = 0;
        }
        if(!this.triangle.enabled){
            this.triangle.lengthValue = 0;
        }
        if(!this.noise.enabled){
            this.noise.lengthValue = 0;
        }
        if(!this.dmc.enabled){
            this.dmc.currentLength = 0;
        } else {
            if(this.dmc.currentLength == 0){
                this.dmc.restart();
            }
        }
    }

    public void writeFrameCounter(byte value) {
        this.framePeriod = (byte) (4 + (value>>7)&1);
        this.frameIRQ = ((value>>6)&1) == 0;
        // this.frameValue = 0
        // if this.framePeriod == 5 {
        // 	this.stepEnvelope()
        // 	this.stepSweep()
        // 	this.stepLength()
        // }
    }
    
    
    class Pulse {
        boolean enabled;
        byte channel;
        boolean lengthEnabled;
        byte lengthValue;
        int timerPeriod;
        int timerValue;
        byte dutyMode;
        byte dutyValue;
        boolean sweepReload;
        boolean sweepEnabled;
        boolean sweepNegate;
        byte sweepShift;
        byte sweepPeriod;
        byte sweepValue;
        boolean envelopeEnabled;
        boolean envelopeLoop;
        boolean envelopeStart;
        byte envelopePeriod;
        byte envelopeValue;
        byte envelopeVolume;
        byte constantVolume;

        public void writeControl(byte value){
            this.dutyMode = (byte) ((value >> 6) & 3);
            this.lengthEnabled = ((value>>5)&1) == 0;
            this.envelopeLoop = ((value>>5)&1) == 1;
            this.envelopeEnabled = ((value>>4)&1) == 0;
            this.envelopePeriod = (byte) (value & 15);
            this.constantVolume = (byte) (value & 15);
            this.envelopeStart = true;
        }


        public void writeSweep(byte value) {
            this.sweepEnabled = ((value>>7)&1) == 1;
            this.sweepPeriod = (byte) ((value >> 4) & 7);
            this.sweepNegate = ((value>>3)&1) == 1;
            this.sweepShift = (byte) (value & 7);
            this.sweepReload = true;
        }

        public void writeTimerLow(byte value) {
            this.timerPeriod = (this.timerPeriod & 0xFF00) | (value);
        }

        public void writeTimerHigh(byte value) {
            this.lengthValue = (byte) lengthTable[value>>3];
            this.timerPeriod = (this.timerPeriod & 0x00FF) | ((value&7) << 8);
            this.envelopeStart = true;
            this.dutyValue = 0;
        }

        public void stepTimer() {
            if(this.timerValue == 0){
                this.timerValue = this.timerPeriod;
                this.dutyValue = (byte) ((this.dutyValue + 1) % 8);
            } else {
                this.timerValue--;
            }
        }

        public void stepEnvelope() {
            if(this.envelopeStart){
                this.envelopeVolume = 15;
                this.envelopeValue = this.envelopePeriod;
                this.envelopeStart = false;
            } else if(this.envelopeValue > 0){
                this.envelopeValue--;
            } else {
                if(this.envelopeVolume > 0){
                    this.envelopeVolume--;
                } else if(this.envelopeLoop){
                    this.envelopeVolume = 15;
                }
                this.envelopeValue = this.envelopePeriod;
            }
        }

        public void stepSweep() {
            if(this.sweepReload){
                if(this.sweepEnabled && this.sweepValue == 0){
                    this.sweep();
                }
                this.sweepValue = this.sweepPeriod;
                this.sweepReload = false;
            } else if(this.sweepValue > 0){
                this.sweepValue--;
            } else {
                if(this.sweepEnabled){
                    this.sweep();
                }
                this.sweepValue = this.sweepPeriod;
            }
        }

        public void stepLength() {
            if(this.lengthEnabled && this.lengthValue > 0){
                this.lengthValue--;
            }
        }

        public void sweep() {
            int delta = this.timerPeriod >> this.sweepShift;
            if(this.sweepNegate){
                this.timerPeriod -= delta;
                if(this.channel == 1){
                    this.timerPeriod--;
                }
            } else {
                this.timerPeriod += delta;
            }
        }

        public byte output(){
            if(!this.enabled){
                return 0;
            }
            if(this.lengthValue == 0){
                return 0;
            }
            if(dutyTable[this.dutyMode][this.dutyValue] == 0){
                return 0;
            }
            if(this.timerPeriod < 8 || this.timerPeriod > 0x7FF){
                return 0;
            }
            // if(!this.sweepNegate && this.timerPeriod+(this.timerPeriod>>this.sweepShift) > 0x7FF {
            // 	return 0
            // }
            if(this.envelopeEnabled){
                return this.envelopeVolume;
            } else {
                return this.constantVolume;
            }
        }
    }

    class Triangle {
        boolean enabled;
        boolean lengthEnabled;
        byte lengthValue;
        int timerPeriod;
        int timerValue;
        byte dutyValue;
        byte counterPeriod;
        byte counterValue;
        boolean counterReload;
        
        public void writeControl(byte value) {
            this.lengthEnabled = ((value>>7)&1) == 0;
            this.counterPeriod = (byte) (value & 0x7F);
        }

        public void writeTimerLow(byte value) {
            this.timerPeriod = (this.timerPeriod & 0xFF00) | (value);
        }

        public void writeTimerHigh(byte value) {
            this.lengthValue = (byte) lengthTable[value>>3];
            this.timerPeriod = (this.timerPeriod & 0x00FF) | ((value&7) << 8);
            this.timerValue = this.timerPeriod;
            this.counterReload = true;
        }

        public void stepTimer() {
            if(this.timerValue == 0){
                this.timerValue = this.timerPeriod;
                if(this.lengthValue > 0 && this.counterValue > 0){
                    this.dutyValue = (byte) ((this.dutyValue + 1) % 32);
                }
            } else {
                this.timerValue--;
            }
        }

        public void stepLength() {
            if(this.lengthEnabled && this.lengthValue > 0){
                this.lengthValue--;
            }
        }

        public void stepCounter() {
            if(this.counterReload){
                this.counterValue = this.counterPeriod;
            } else if(this.counterValue > 0){
                this.counterValue--;
            }
            if(this.lengthEnabled){
                this.counterReload = false;
            }
        }

        public byte output() {
            if(!this.enabled){
                return 0;
            }
            if(this.lengthValue == 0){
                return 0;
            }
            if(this.counterValue == 0){
                return 0;
            }
            return triangleTable[this.dutyValue];
        }
    }

    class Noise {
        boolean enabled;
        boolean mode;
        int shiftRegister;
        boolean lengthEnabled;
        byte lengthValue;
        int timerPeriod;
        int timerValue;
        boolean envelopeEnabled;
        boolean envelopeLoop;
        boolean envelopeStart;
        byte envelopePeriod;
        byte envelopeValue;
        byte envelopeVolume;
        byte constantVolume;


        public void writeControl(byte value) {
            this.lengthEnabled = ((value>>5)&1) == 0;
            this.envelopeLoop = ((value>>5)&1) == 1;
            this.envelopeEnabled = ((value>>4)&1) == 0;
            this.envelopePeriod = (byte) (value & 15);
            this.constantVolume = (byte) (value & 15);
            this.envelopeStart = true;
        }

        public void writePeriod(byte value) {
            this.mode = (value&0x80) == 0x80;
            this.timerPeriod = noiseTable[value&0x0F];
        }

        public void writeLength(byte value) {
            this.lengthValue = (byte) lengthTable[value>>3];
            this.envelopeStart = true;
        }

        public void stepTimer() {
            if(this.timerValue == 0){
                this.timerValue = this.timerPeriod;
                byte shift;
                if(this.mode){
                    shift = 6;
                } else {
                    shift = 1;
                }
                byte b1 = (byte) (this.shiftRegister & 1);
                byte b2 = (byte) ((this.shiftRegister >> shift) & 1);
                this.shiftRegister >>= 1;
                this.shiftRegister |= (b1 ^ b2) << 14;
            } else {
                this.timerValue--;
            }
        }

        public void stepEnvelope() {
            if(this.envelopeStart){
                this.envelopeVolume = 15;
                this.envelopeValue = this.envelopePeriod;
                this.envelopeStart = false;
            } else if(this.envelopeValue > 0){
                this.envelopeValue--;
            } else {
                if(this.envelopeVolume > 0){
                    this.envelopeVolume--;
                } else if(this.envelopeLoop){
                    this.envelopeVolume = 15;
                }
                this.envelopeValue = this.envelopePeriod;
            }
        }

        public void stepLength() {
            if(this.lengthEnabled && this.lengthValue > 0){
                this.lengthValue--;
            }
        }

        public byte output() {
            if(!this.enabled){
                return 0;
            }
            if(this.lengthValue == 0){
                return 0;
            }
            if((this.shiftRegister&1) == 1){
                return 0;
            }
            if(this.envelopeEnabled){
                return this.envelopeVolume;
            } else {
                return this.constantVolume;
            }
        }
    }

    class DMC {
        CPU cpu;
        boolean enabled;
        byte value;
        int sampleAddress;
        int sampleLength;
        int currentAddress;
        int currentLength;
        byte shiftRegister;
        byte bitCount;
        byte tickPeriod;
        byte tickValue;
        boolean loop;
        boolean irq;
        
        public DMC(CPU cpu){
            this.cpu = cpu;
        }
        
        public void writeControl(byte value) {
            this.irq = (value&0x80) == 0x80;
            this.loop = (value&0x40) == 0x40;
            this.tickPeriod = (byte) dmcTable[value&0x0F];
        }

        public void writeValue(byte value) {
            this.value = (byte) (value & 0x7F);
        }

        public void writeAddress(byte value) {
            // Sample address = %11AAAAAA.AA000000
            this.sampleAddress = 0xC000 | (value << 6);
        }

        public void writeLength(byte value) {
            // Sample length = %0000LLLL.LLLL0001
            this.sampleLength = (value << 4) | 1;
        }

        public void restart() {
            this.currentAddress = this.sampleAddress;
            this.currentLength = this.sampleLength;
        }

        public void stepTimer() {
            if(!this.enabled){
                return;
            }
            this.stepReader();
            if(this.tickValue == 0){
                this.tickValue = this.tickPeriod;
                this.stepShifter();
            } else {
                this.tickValue--;
            }
        }

        public void stepReader() {
            if(this.currentLength > 0 && this.bitCount == 0){
                this.cpu.stall += 4;
                this.shiftRegister = this.cpu.Memory.Read(this.currentAddress);
                this.bitCount = 8;
                this.currentAddress++;
                if(this.currentAddress == 0){
                    this.currentAddress = 0x8000;
                }
                this.currentLength--;
                if(this.currentLength == 0 && this.loop){
                    this.restart();
                }
            }
        }

        public void stepShifter() {
            if(this.bitCount == 0){
                return;
            }
            if((this.shiftRegister&1) == 1){
                if(this.value <= 125){
                    this.value += 2;
                }
            } else {
                if(this.value >= 2){
                    this.value -= 2;
                }
            }
            this.shiftRegister >>= 1;
            this.bitCount--;
        }

        public byte output(){
            return this.value;
        }

    }
}
