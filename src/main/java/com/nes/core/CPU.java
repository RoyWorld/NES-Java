package com.nes.core;

import com.nes.core.memory.CPUMemory;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class CPU {

    private final int interruptNone = 1;
    private final int interruptNMI = 2;
    private final int interruptIRQ = 3;

    private final int modeAbsolute =1;
    private final int modeAbsoluteX =2;
    private final int modeAbsoluteY =3;
    private final int modeAccumulator =4;
    private final int modeImmediate =5;
    private final int modeImplied =6;
    private final int modeIndexedIndirect =7;
    private final int modeIndirect =8;
    private final int modeIndirectIndexed =9;
    private final int modeRelative =10;
    private final int modeZeroPage =11;
    private final int modeZeroPageX =12;
    private final int modeZeroPageY =13;

    // instructionModes indicates the addressing mode for each instruction
    byte[] instructionModes = new byte[]{
        6, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
        1, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
        6, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
        6, 7, 6, 7, 11, 11, 11, 11, 6, 5, 4, 5, 8, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
        5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 13, 13, 6, 3, 6, 3, 2, 2, 3, 3,
        5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 13, 13, 6, 3, 6, 3, 2, 2, 3, 3,
        5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2,
        5, 7, 5, 7, 11, 11, 11, 11, 6, 5, 6, 5, 1, 1, 1, 1,
        10, 9, 6, 9, 12, 12, 12, 12, 6, 3, 6, 3, 2, 2, 2, 2
    };

    // instructionSizes indicates the size of each instruction in bytes
    byte[] instructionSizes = new byte[]{
        1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
        3, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
        1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
        1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 0, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 0, 3, 0, 0,
        2, 2, 2, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0,
        2, 2, 0, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0,
    };

    // instructionCycles indicates the number of cycles used by each instruction,
// not including conditional cycles
    byte[] instructionCycles = new byte[]{
        7, 6, 2, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6,
        2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
        6, 6, 2, 8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6,
        2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
        6, 6, 2, 8, 3, 3, 5, 5, 3, 2, 2, 2, 3, 4, 6, 6,
        2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
        6, 6, 2, 8, 3, 3, 5, 5, 4, 2, 2, 2, 5, 4, 6, 6,
        2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
        2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
        2, 6, 2, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5,
        2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
        2, 5, 2, 5, 4, 4, 4, 4, 2, 4, 2, 4, 4, 4, 4, 4,
        2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
        2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
        2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
        2, 5, 2, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    };

    // instructionPageCycles indicates the number of cycles used by each
// instruction when a page is crossed
    byte[] instructionPageCycles = new byte[]{
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0,
    };

    // instructionNames indicates the name of each instruction
    String[] instructionNames = new String[]{
        "BRK", "ORA", "KIL", "SLO", "NOP", "ORA", "ASL", "SLO",
        "PHP", "ORA", "ASL", "ANC", "NOP", "ORA", "ASL", "SLO",
        "BPL", "ORA", "KIL", "SLO", "NOP", "ORA", "ASL", "SLO",
        "CLC", "ORA", "NOP", "SLO", "NOP", "ORA", "ASL", "SLO",
        "JSR", "AND", "KIL", "RLA", "BIT", "AND", "ROL", "RLA",
        "PLP", "AND", "ROL", "ANC", "BIT", "AND", "ROL", "RLA",
        "BMI", "AND", "KIL", "RLA", "NOP", "AND", "ROL", "RLA",
        "SEC", "AND", "NOP", "RLA", "NOP", "AND", "ROL", "RLA",
        "RTI", "EOR", "KIL", "SRE", "NOP", "EOR", "LSR", "SRE",
        "PHA", "EOR", "LSR", "ALR", "JMP", "EOR", "LSR", "SRE",
        "BVC", "EOR", "KIL", "SRE", "NOP", "EOR", "LSR", "SRE",
        "CLI", "EOR", "NOP", "SRE", "NOP", "EOR", "LSR", "SRE",
        "RTS", "ADC", "KIL", "RRA", "NOP", "ADC", "ROR", "RRA",
        "PLA", "ADC", "ROR", "ARR", "JMP", "ADC", "ROR", "RRA",
        "BVS", "ADC", "KIL", "RRA", "NOP", "ADC", "ROR", "RRA",
        "SEI", "ADC", "NOP", "RRA", "NOP", "ADC", "ROR", "RRA",
        "NOP", "STA", "NOP", "SAX", "STY", "STA", "STX", "SAX",
        "DEY", "NOP", "TXA", "XAA", "STY", "STA", "STX", "SAX",
        "BCC", "STA", "KIL", "AHX", "STY", "STA", "STX", "SAX",
        "TYA", "STA", "TXS", "TAS", "SHY", "STA", "SHX", "AHX",
        "LDY", "LDA", "LDX", "LAX", "LDY", "LDA", "LDX", "LAX",
        "TAY", "LDA", "TAX", "LAX", "LDY", "LDA", "LDX", "LAX",
        "BCS", "LDA", "KIL", "LAX", "LDY", "LDA", "LDX", "LAX",
        "CLV", "LDA", "TSX", "LAS", "LDY", "LDA", "LDX", "LAX",
        "CPY", "CMP", "NOP", "DCP", "CPY", "CMP", "DEC", "DCP",
        "INY", "CMP", "DEX", "AXS", "CPY", "CMP", "DEC", "DCP",
        "BNE", "CMP", "KIL", "DCP", "NOP", "CMP", "DEC", "DCP",
        "CLD", "CMP", "NOP", "DCP", "NOP", "CMP", "DEC", "DCP",
        "CPX", "SBC", "NOP", "ISC", "CPX", "SBC", "INC", "ISC",
        "INX", "SBC", "NOP", "SBC", "CPX", "SBC", "INC", "ISC",
        "BEQ", "SBC", "KIL", "ISC", "NOP", "SBC", "INC", "ISC",
        "SED", "SBC", "NOP", "ISC", "NOP", "SBC", "INC", "ISC",
    };

    @FunctionalInterface
    interface Instruction{
        void exec(StepInfo info);
    }

    com.nes.core.memory.Memory Memory;// memory interface
    long Cycles;// number of cycles
    int PC;// program counter
    byte SP;// stack pointer
    byte A;// accumulator
    byte X;// x register
    byte Y;// y register
    byte C;// carry flag
    byte Z;// zero flag
    byte I;// interrupt disable flag
    byte D;// decimal mode flag
    byte B;// break command flag
    byte U;// unused flag
    byte V;// overflow flag
    byte N;// negative flag
    byte interrupt;// interrupt type to perform
    int stall;// number of cycles to stall
    Instruction[] table;

    // StepInfo contains information that the instruction functions use
    class StepInfo{
        int address;
        int pc;
        byte mode;

        public StepInfo(int address, int pc, byte mode) {
            this.address = address;
            this.pc = pc;
            this.mode = mode;
        }
    }
    
    public CPU(Console console){
        Memory = new CPUMemory(console);
        createTable();
        Reset();
    }

    private void createTable() {
        table = new Instruction[]{
                (info)->brk(info), (info)->ora(info), (info)->kil(info), (info)->slo(info), (info)->nop(info), (info)->ora(info), (info)->asl(info), (info)->slo(info),
                (info)->php(info), (info)->ora(info), (info)->asl(info), (info)->anc(info), (info)->nop(info), (info)->ora(info), (info)->asl(info), (info)->slo(info),
                (info)->bpl(info), (info)->ora(info), (info)->kil(info), (info)->slo(info), (info)->nop(info), (info)->ora(info), (info)->asl(info), (info)->slo(info),
                (info)->clc(info), (info)->ora(info), (info)->nop(info), (info)->slo(info), (info)->nop(info), (info)->ora(info), (info)->asl(info), (info)->slo(info),
                (info)->jsr(info), (info)->and(info), (info)->kil(info), (info)->rla(info), (info)->bit(info), (info)->and(info), (info)->rol(info), (info)->rla(info),
                (info)->plp(info), (info)->and(info), (info)->rol(info), (info)->anc(info), (info)->bit(info), (info)->and(info), (info)->rol(info), (info)->rla(info),
                (info)->bmi(info), (info)->and(info), (info)->kil(info), (info)->rla(info), (info)->nop(info), (info)->and(info), (info)->rol(info), (info)->rla(info),
                (info)->sec(info), (info)->and(info), (info)->nop(info), (info)->rla(info), (info)->nop(info), (info)->and(info), (info)->rol(info), (info)->rla(info),
                (info)->rti(info), (info)->eor(info), (info)->kil(info), (info)->sre(info), (info)->nop(info), (info)->eor(info), (info)->lsr(info), (info)->sre(info),
                (info)->pha(info), (info)->eor(info), (info)->lsr(info), (info)->alr(info), (info)->jmp(info), (info)->eor(info), (info)->lsr(info), (info)->sre(info),
                (info)->bvc(info), (info)->eor(info), (info)->kil(info), (info)->sre(info), (info)->nop(info), (info)->eor(info), (info)->lsr(info), (info)->sre(info),
                (info)->cli(info), (info)->eor(info), (info)->nop(info), (info)->sre(info), (info)->nop(info), (info)->eor(info), (info)->lsr(info), (info)->sre(info),
                (info)->rts(info), (info)->adc(info), (info)->kil(info), (info)->rra(info), (info)->nop(info), (info)->adc(info), (info)->ror(info), (info)->rra(info),
                (info)->pla(info), (info)->adc(info), (info)->ror(info), (info)->arr(info), (info)->jmp(info), (info)->adc(info), (info)->ror(info), (info)->rra(info),
                (info)->bvs(info), (info)->adc(info), (info)->kil(info), (info)->rra(info), (info)->nop(info), (info)->adc(info), (info)->ror(info), (info)->rra(info),
                (info)->sei(info), (info)->adc(info), (info)->nop(info), (info)->rra(info), (info)->nop(info), (info)->adc(info), (info)->ror(info), (info)->rra(info),
                (info)->nop(info), (info)->sta(info), (info)->nop(info), (info)->sax(info), (info)->sty(info), (info)->sta(info), (info)->stx(info), (info)->sax(info),
                (info)->dey(info), (info)->nop(info), (info)->txa(info), (info)->xaa(info), (info)->sty(info), (info)->sta(info), (info)->stx(info), (info)->sax(info),
                (info)->bcc(info), (info)->sta(info), (info)->kil(info), (info)->ahx(info), (info)->sty(info), (info)->sta(info), (info)->stx(info), (info)->sax(info),
                (info)->tya(info), (info)->sta(info), (info)->txs(info), (info)->tas(info), (info)->shy(info), (info)->sta(info), (info)->shx(info), (info)->ahx(info),
                (info)->ldy(info), (info)->lda(info), (info)->ldx(info), (info)->lax(info), (info)->ldy(info), (info)->lda(info), (info)->ldx(info), (info)->lax(info),
                (info)->tay(info), (info)->lda(info), (info)->tax(info), (info)->lax(info), (info)->ldy(info), (info)->lda(info), (info)->ldx(info), (info)->lax(info),
                (info)->bcs(info), (info)->lda(info), (info)->kil(info), (info)->lax(info), (info)->ldy(info), (info)->lda(info), (info)->ldx(info), (info)->lax(info),
                (info)->clv(info), (info)->lda(info), (info)->tsx(info), (info)->las(info), (info)->ldy(info), (info)->lda(info), (info)->ldx(info), (info)->lax(info),
                (info)->cpy(info), (info)->cmp(info), (info)->nop(info), (info)->dcp(info), (info)->cpy(info), (info)->cmp(info), (info)->dec(info), (info)->dcp(info),
                (info)->iny(info), (info)->cmp(info), (info)->dex(info), (info)->axs(info), (info)->cpy(info), (info)->cmp(info), (info)->dec(info), (info)->dcp(info),
                (info)->bne(info), (info)->cmp(info), (info)->kil(info), (info)->dcp(info), (info)->nop(info), (info)->cmp(info), (info)->dec(info), (info)->dcp(info),
                (info)->cld(info), (info)->cmp(info), (info)->nop(info), (info)->dcp(info), (info)->nop(info), (info)->cmp(info), (info)->dec(info), (info)->dcp(info),
                (info)->cpx(info), (info)->sbc(info), (info)->nop(info), (info)->isc(info), (info)->cpx(info), (info)->sbc(info), (info)->inc(info), (info)->isc(info),
                (info)->inx(info), (info)->sbc(info), (info)->nop(info), (info)->sbc(info), (info)->cpx(info), (info)->sbc(info), (info)->inc(info), (info)->isc(info),
                (info)->beq(info), (info)->sbc(info), (info)->kil(info), (info)->isc(info), (info)->nop(info), (info)->sbc(info), (info)->inc(info), (info)->isc(info),
                (info)->sed(info), (info)->sbc(info), (info)->nop(info), (info)->isc(info), (info)->nop(info), (info)->sbc(info), (info)->inc(info), (info)->isc(info),
        };
    }

    // Reset resets the CPU to its initial powerup state
    public void Reset() {
        this.PC = this.Read16(0xFFFC);
        this.SP = (byte) 0xFD;
        this.SetFlags((byte) 0x24);
    }


    // PrintInstruction prints the current CPU state
    public void PrintInstruction() {
        byte opcode = this.Memory.Read(this.PC);
        byte bytes = instructionSizes[opcode];
        String name = instructionNames[opcode];
        String w0 = String.format("%02X", this.Memory.Read(this.PC+0));
        String w1 = String.format("%02X", this.Memory.Read(this.PC+1));
        String w2 = String.format("%02X", this.Memory.Read(this.PC+2));
        if(bytes < 2){
            w1 = "  ";
        }
        if(bytes < 3){
            w2 = "  ";
        }
        System.out.println(String.format("%4X  %s %s %s  %s %28s"+
                        "A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC:%3d\n",
                this.PC, w0, w1, w2, name, "",
                this.A, this.X, this.Y, this.Flags(), this.SP, (this.Cycles*3)%341));

    }

    // pagesDiffer returns true if the two addresses reference different pages
    public boolean pagesDiffer(int a, int b)  {
        return (a&0xFF00) != (b&0xFF00);
    }

    // addBranchCycles adds a cycle for taking a branch and adds another cycle
// if the branch jumps to a new page
    public void addBranchCycles(StepInfo info) {
        this.Cycles++;
        if(pagesDiffer(info.pc, info.address)){
            this.Cycles++;
        }
    }

    public void compare(byte a, byte b) {
        this.setZN((byte) (a - b));
        if(a >= b){
            this.C = 1;
        } else {
            this.C = 0;
        }
    }

    // Read16 reads two bytes using Read to return a double-word byte value
    public int Read16(int address) {
       int lo = this.Memory.Read(address);
       int hi = this.Memory.Read(address + 1);
        return hi<<8 | lo;
    }

    // read16bug emulates a 6502 bug that caused the low byte to wrap without
// incrementing the high byte
    public int read16bug(int address) {
       int a = address;
       int b = (a & 0xFF00) | (a+1);
       int lo = this.Memory.Read(a);
       int hi = this.Memory.Read(b);
        return hi<<8 | lo;
    }

    // push pushes a byte onto the stack
    public void push(byte value) {
        this.Memory.Write(0x100|this.SP, value);
        this.SP--;
    }

    // pull pops a byte from the stack
    public byte pull() {
        this.SP++;
        return this.Memory.Read(0x100 | this.SP);
    }

    // push16 pushes two bytes onto the stack
    public void push16(int value) {
        byte hi = (byte) (value >> 8);
        byte lo = (byte) (value & 0xFF);
        this.push(hi);
        this.push(lo);
    }

    // pull16 pops two bytes from the stack
    public int pull16(){
        int lo = this.pull();
        int hi = this.pull();
        return hi<<8 | lo;
    }

    // Flags returns the processor status flags
    public byte Flags(){
        byte flags = 0;
        flags |= this.C << 0;
        flags |= this.Z << 1;
        flags |= this.I << 2;
        flags |= this.D << 3;
        flags |= this.B << 4;
        flags |= this.U << 5;
        flags |= this.V << 6;
        flags |= this.N << 7;
        return flags;
    }

    // SetFlags sets the processor status flags
    public void SetFlags(byte flags) {
        this.C = (byte) ((flags >> 0) & 1);;
        this.Z = (byte) ((flags >> 1) & 1);;
        this.I = (byte) ((flags >> 2) & 1);;
        this.D = (byte) ((flags >> 3) & 1);;
        this.B = (byte) ((flags >> 4) & 1);;
        this.U = (byte) ((flags >> 5) & 1);;
        this.V = (byte) ((flags >> 6) & 1);;
        this.N = (byte) ((flags >> 7) & 1);;
    }

    // setZ sets the zero flag if the argument is zero
    public void setZ(byte value) {
        if(value == 0){
            this.Z = 1;
        } else {
            this.Z = 0;
        }
    }

    // setN sets the negative flag if the argument is negative (high bit is set)
    public void setN(byte value) {
        if((value&0x80) != 0){
            this.N = 1;
        } else {
            this.N = 0;
        }
    }

    // setZN sets the zero flag and the negative flag
    public void setZN(byte value) {
        this.setZ(value);
        this.setN(value);
    }

    // triggerNMI causes a non-maskable interrupt to occur on the next cycle
    public void triggerNMI() {
        this.interrupt = (byte) interruptNMI;
    }

    // triggerIRQ causes an IRQ interrupt to occur on the next cycle
    public void triggerIRQ() {
        if(this.I == 0){
            this.interrupt = (byte) interruptIRQ;
        }
    }

    // Step executes a single CPU instruction
    public int Step(){
        if(this.stall > 0){
            this.stall--;
            return 1;
        }

       long cycles = this.Cycles;

        switch(this.interrupt){
            case interruptNMI:
                this.nmi();
            case interruptIRQ:
                this.irq();
        }
        this.interrupt = interruptNone;

        int opcode = this.Memory.Read(this.PC);
        int mode = instructionModes[opcode];

        int address = 0;
        boolean pageCrossed = false;
        switch(mode){
            case modeAbsolute:
                address = this.Read16(this.PC + 1);
            case modeAbsoluteX:
                address = this.Read16(this.PC+1) + (this.X);
                pageCrossed = pagesDiffer(address-(this.X), address);
            case modeAbsoluteY:
                address = this.Read16(this.PC+1) + (this.Y);
                pageCrossed = pagesDiffer(address-(this.Y), address);
            case modeAccumulator:
                address = 0;
            case modeImmediate:
                address = this.PC + 1;
            case modeImplied:
                address = 0;
            case modeIndexedIndirect:
                address = this.read16bug((this.Memory.Read(this.PC+1) + this.X));
            case modeIndirect:
                address = this.read16bug(this.Read16(this.PC + 1));
            case modeIndirectIndexed:
                address = this.read16bug((this.Memory.Read(this.PC+1))) + (this.Y);
                pageCrossed = pagesDiffer(address-(this.Y), address);
            case modeRelative:
                int offset = (this.Memory.Read(this.PC + 1));
                if(offset < 0x80){
                    address = this.PC + 2 + offset;
                } else {
                    address = this.PC + 2 + offset - 0x100;
                }
            case modeZeroPage:
                address = (this.Memory.Read(this.PC + 1));
            case modeZeroPageX:
                address = (this.Memory.Read(this.PC+1) + this.X);
            case modeZeroPageY:
                address = (this.Memory.Read(this.PC+1) + this.Y);
        }

        this.PC += (instructionSizes[opcode]);
        this.Cycles += (instructionCycles[opcode]);
        if(pageCrossed){
            this.Cycles += (instructionPageCycles[opcode]);
        }
        StepInfo info = new StepInfo(address, this.PC, (byte) mode);
        this.table[opcode].exec(info);

        return (int) (this.Cycles - cycles);
    }

    // NMI - Non-Maskable Interrupt
    public void nmi() {
        this.push16(this.PC);
        this.php(null);
        this.PC = this.Read16(0xFFFA);
        this.I = 1;
        this.Cycles += 7;
    }

    // IRQ - IRQ Interrupt
    public void irq() {
        this.push16(this.PC);
        this.php(null);
        this.PC = this.Read16(0xFFFE);
        this.I = 1;
        this.Cycles += 7;
    }

    // ADC - Add with Carry
    public void adc(StepInfo info) {
        byte a = this.A;
        byte b = this.Memory.Read(info.address);
        byte c = this.C;
        this.A = (byte) (a + b + c);
        this.setZN(this.A);
        if(a+b+c > 0xFF){
            this.C = 1;
        } else {
            this.C = 0;
        }
        if(((a^b)&0x80) == 0 && ((a^this.A)&0x80) != 0){
            this.V = 1;
        } else {
            this.V = 0;
        }
    }

    // AND - Logical AND
    public void and(StepInfo info) {
        this.A = (byte) (this.A & this.Memory.Read(info.address));
        this.setZN(this.A);
    }

    // ASL - Arithmetic Shift Left
    public void asl(StepInfo info) {
        if(info.mode == modeAccumulator){
            this.C = (byte) ((this.A >> 7) & 1);
            this.A <<= 1;
            this.setZN(this.A);
        } else {
            byte value = this.Memory.Read(info.address);
            this.C = (byte) ((value >> 7) & 1);
            value <<= 1;
            this.Memory.Write(info.address, value);
            this.setZN(value);
        }
    }

    // BCC - Branch if Carry Clear
    public void bcc(StepInfo info) {
        if(this.C == 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BCS - Branch if Carry Set
    public void bcs(StepInfo info) {
        if(this.C != 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BEQ - Branch if Equal
    public void beq(StepInfo info) {
        if(this.Z != 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BIT - Bit Test
    public void bit(StepInfo info) {
        byte value = this.Memory.Read(info.address);
        this.V = (byte) ((value >> 6) & 1);
        this.setZ((byte) (value & this.A));
        this.setN(value);
    }

    // BMI - Branch if Minus
    public void bmi(StepInfo info) {
        if(this.N != 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BNE - Branch if Not Equal
    public void bne(StepInfo info) {
        if(this.Z == 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BPL - Branch if Positive
    public void bpl(StepInfo info) {
        if(this.N == 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BRK - Force Interrupt
    public void brk(StepInfo info) {
        this.push16(this.PC);
        this.php(info);
        this.sei(info);
        this.PC = this.Read16(0xFFFE);
    }

    // BVC - Branch if Overflow Clear
    public void bvc(StepInfo info) {
        if(this.V == 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // BVS - Branch if Overflow Set
    public void bvs(StepInfo info) {
        if(this.V != 0){
            this.PC = info.address;
            this.addBranchCycles(info);
        }
    }

    // CLC - Clear Carry Flag
    public void clc(StepInfo info) {
        this.C = 0;
    }

    // CLD - Clear Decimal Mode
    public void cld(StepInfo info) {
        this.D = 0;
    }

    // CLI - Clear Interrupt Disable
    public void cli(StepInfo info) {
        this.I = 0;
    }

    // CLV - Clear Overflow Flag
    public void clv(StepInfo info) {
        this.V = 0;
    }

    // CMP - Compare
    public void cmp(StepInfo info) {
        byte value = this.Memory.Read(info.address);
        this.compare(this.A, value);
    }

    // CPX - Compare X Register
    public void cpx(StepInfo info) {
        byte value = this.Memory.Read(info.address);
        this.compare(this.X, value);
    }

    // CPY - Compare Y Register
    public void cpy(StepInfo info) {
        byte value = this.Memory.Read(info.address);
        this.compare(this.Y, value);
    }

    // DEC - Decrement memory
    public void dec(StepInfo info) {
        byte value = (byte) (this.Memory.Read(info.address) - 1);
        this.Memory.Write(info.address, value);
        this.setZN(value);
    }

    // DEX - Decrement X Register
    public void dex(StepInfo info) {
        this.X--;
        this.setZN(this.X);
    }

    // DEY - Decrement Y Register
    public void dey(StepInfo info) {
        this.Y--;
        this.setZN(this.Y);
    }

    // EOR - Exclusive OR
    public void eor(StepInfo info) {
        this.A = (byte) (this.A ^ this.Memory.Read(info.address));
        this.setZN(this.A);
    }

    // INC - Increment Mefuncmory
    public void inc(StepInfo info) {
        byte value = (byte) (this.Memory.Read(info.address) + 1);
        this.Memory.Write(info.address, value);
        this.setZN(value);
    }

    // INX - Increment X Register
    public void inx(StepInfo info) {
        this.X++;
        this.setZN(this.X);
    }

    // INY - Increment Y Register
    public void iny(StepInfo info) {
        this.Y++;
        this.setZN(this.Y);
    }

    // JMP - Jump
    public void jmp(StepInfo info) {
        this.PC = info.address;
    }

    // JSR - Jump to Subroutine
    public void jsr(StepInfo info) {
        this.push16(this.PC - 1);
        this.PC = info.address;
    }

    // LDA - Load Accumulator
    public void lda(StepInfo info) {
        this.A = this.Memory.Read(info.address);
        this.setZN(this.A);
    }

    // LDX - Load X Register
    public void ldx(StepInfo info) {
        this.X = this.Memory.Read(info.address);
        this.setZN(this.X);
    }

    // LDY - Load Y Register
    public void ldy(StepInfo info) {
        this.Y = this.Memory.Read(info.address);
        this.setZN(this.Y);
    }

    // LSR - Logical Shift Right
    public void lsr(StepInfo info) {
        if(info.mode == modeAccumulator){
            this.C = (byte) (this.A & 1);
            this.A >>= 1;
            this.setZN(this.A);
        } else {
            byte value = this.Memory.Read(info.address);
            this.C = (byte) (value & 1);
            value >>= 1;
            this.Memory.Write(info.address, value);
            this.setZN(value);
        }
    }

    // NOP - No Operation
    public void nop(StepInfo info) {
    }

    // ORA - Logical Inclusive OR
    public void ora(StepInfo info) {
        this.A = (byte) (this.A | this.Memory.Read(info.address));
        this.setZN(this.A);
    }

    // PHA - Push Accumulator
    public void pha(StepInfo info) {
        this.push(this.A);
    }

    // PHP - Push Processor Status
    public void php(StepInfo info) {
        this.push((byte) (this.Flags() | 0x10));
    }

    // PLA - Pull Accumulator
    public void pla(StepInfo info) {
        this.A = this.pull();
        this.setZN(this.A);
    }

    // PLP - Pull Processor Status
    public void plp(StepInfo info) {
        this.SetFlags((byte) (this.pull()&0xEF | 0x20));
    }

    // ROL - Rotate Left
    public void rol(StepInfo info) {
        if(info.mode == modeAccumulator){
            byte c = this.C;
            this.C = (byte) ((this.A >> 7) & 1);
            this.A = (byte) ((this.A << 1) | c);
            this.setZN(this.A);
        } else {
            byte c = this.C;
            byte value = this.Memory.Read(info.address);
            this.C = (byte) ((value >> 7) & 1);
            value = (byte) ((value << 1) | c);
            this.Memory.Write(info.address, value);
            this.setZN(value);
        }
    }

    // ROR - Rotate Right
    public void ror(StepInfo info) {
        if(info.mode == modeAccumulator){
            byte c = this.C;
            this.C = (byte) (this.A & 1);
            this.A = (byte) ((this.A >> 1) | (c << 7));
            this.setZN(this.A);
        } else {
            byte c = this.C;
            byte value = this.Memory.Read(info.address);
            this.C = (byte) (value & 1);
            value = (byte) ((value >> 1) | (c << 7));
            this.Memory.Write(info.address, value);
            this.setZN(value);
        }
    }

    // RTI - Return from Interrupt
    public void rti(StepInfo info) {
        this.SetFlags((byte) (this.pull()&0xEF | 0x20));
        this.PC = this.pull16();
    }

    // RTS - Return from Subroutine
    public void rts(StepInfo info) {
        this.PC = this.pull16() + 1;
    }

    // SBC - Subtract with Carry
    public void sbc(StepInfo info) {
        byte a = this.A;
        byte b = this.Memory.Read(info.address);
        byte c = this.C;
        this.A = (byte) (a - b - (1 - c));
        this.setZN(this.A);
        if(a-b-1-c >= 0){
            this.C = 1;
        } else {
            this.C = 0;
        }
        if(((a^b)&0x80) != 0 && ((a^this.A)&0x80) != 0){
            this.V = 1;
        } else {
            this.V = 0;
        }
    }

    // SEC - Set Carry Flag
    public void sec(StepInfo info) {
        this.C = 1;
    }

    // SED - Set Decimal Flag
    public void sed(StepInfo info) {
        this.D = 1;
    }

    // SEI - Set Interrupt Disable
    public void sei(StepInfo info) {
        this.I = 1;
    }

    // STA - Store Accumulator
    public void sta(StepInfo info) {
        this.Memory.Write(info.address, this.A);
    }

    // STX - Store X Register
    public void stx(StepInfo info) {
        this.Memory.Write(info.address, this.X);
    }

    // STY - Store Y Register
    public void sty(StepInfo info) {
        this.Memory.Write(info.address, this.Y);
    }

    // TAX - Transfer Accumulator to X
    public void tax(StepInfo info) {
        this.X = this.A;
        this.setZN(this.X);
    }

    // TAY - Transfer Accumulator to Y
    public void tay(StepInfo info) {
        this.Y = this.A;
        this.setZN(this.Y);
    }

    // TSX - Transfer Stack Pointer to X
    public void tsx(StepInfo info) {
        this.X = this.SP;
        this.setZN(this.X);
    }

    // TXA - Transfer X to Accumulator
    public void txa(StepInfo info) {
        this.A = this.X;
        this.setZN(this.A);
    }

    // TXS - Transfer X to Stack Pointer
    public void txs(StepInfo info) {
        this.SP = this.X;
    }

    // TYA - Transfer Y to Accumulator
    public void tya(StepInfo info) {
        this.A = this.Y;
        this.setZN(this.A);
    }

// illegal opcodes below

    public void ahx(StepInfo info) {
    }

    public void alr(StepInfo info) {
    }

    public void anc(StepInfo info) {
    }

    public void arr(StepInfo info) {
    }

    public void axs(StepInfo info) {
    }

    public void dcp(StepInfo info) {
    }

    public void isc(StepInfo info) {
    }

    public void kil(StepInfo info) {
    }

    public void las(StepInfo info) {
    }

    public void lax(StepInfo info) {
    }

    public void rla(StepInfo info) {
    }

    public void rra(StepInfo info) {
    }

    public void sax(StepInfo info) {
    }

    public void shx(StepInfo info) {
    }

    public void shy(StepInfo info) {
    }

    public void slo(StepInfo info) {
    }

    public void sre(StepInfo info) {
    }

    public void tas(StepInfo info) {
    }

    public void xaa(StepInfo info) {
    }
}
