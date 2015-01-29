/*
 * Codegen.java -- ECO32 code generator
 */


package codegen;

import java.io.*;
import absyn.*;
import table.*;
import types.*;
import varalloc.Varalloc;

public class Codegen implements visitor.Visitor {

  private static final int R_MIN = 8;  // lowest free register
  private static final int R_MAX = 23; // highest free register
  private static final int R_RET = 31; // return address
  private static final int R_FP = 25; // frame pointer
  private static final int R_SP = 29; // stack pointer
  private int freeReg = 8;
  private Table globalTable;
  private PrintWriter outWriter;

  public Codegen(Table t, Writer w) {
    globalTable = t;
    outWriter = new PrintWriter(w);
  }

  private void assemblerProlog() {
    outWriter.format("\t.import\tprinti\n");
    outWriter.format("\t.import\tprintc\n");
    outWriter.format("\t.import\treadi\n");
    outWriter.format("\t.import\treadc\n");
    outWriter.format("\t.import\texit\n");
    outWriter.format("\t.import\ttime\n");
    outWriter.format("\t.import\tclearAll\n");
    outWriter.format("\t.import\tsetPixel\n");
    outWriter.format("\t.import\tdrawLine\n");
    outWriter.format("\t.import\tdrawCircle\n");
    outWriter.format("\t.import\t_indexError\n");
    outWriter.format("\n");
    outWriter.format("\t.code\n");
    outWriter.format("\t.align\t4\n");
  }

  public void genCode(Absyn program) {
    assemblerProlog();
    ((DecList) program).accept(this);

  }

  public void visit(DecList decList) {
    Dec head;

    while (!decList.isEmpty) {
      head = decList.head;

      if (head.getClass() == ProcDec.class) {
        this.visit((ProcDec) head);
      }

      decList = decList.tail;
    }
  }

  public void visit(ProcDec procDec) {
    ProcEntry entry = (ProcEntry) globalTable.lookup(procDec.name);
    String name = procDec.name.toString();
    int frameSize, oldFP, oldRET;

    if (entry.stmCall) {
      frameSize = entry.varAreaSize +
              +2 * Varalloc.refByteSize // Frampointer and Returnadress
              + entry.outAreaSize;
      oldFP = entry.outAreaSize + Varalloc.refByteSize;

    } else {
      frameSize = entry.varAreaSize + Varalloc.refByteSize;
      oldFP = 0;
    }

    oldRET = entry.varAreaSize + 2 * Varalloc.refByteSize;

    outWriter.format("\n\t.export\t" + name + "\n");
    outWriter.format(name + ":\n");
    outWriter.format("\tsub\t$" + R_SP + ",$" + R_SP + "," + frameSize + "\t\t; allocate frame\n");
    outWriter.format("\tstw\t$" + R_FP + ",$" + R_SP + "," + oldFP + "\t\t; save old frame pointer\n");
    outWriter.format("\tadd\t$" + R_FP + ",$" + R_SP + "," + frameSize + "\t\t; setup new frame pointer\n");

    if (entry.stmCall)
      outWriter.format("\tstw\t$" + R_RET + ",$" + R_FP + ",-" + oldRET + "\t\t; save return register\n");

    procDec.body.accept(this);

    if (entry.stmCall)
      outWriter.format("\tldw\t$" + R_RET + ",$" + R_FP + ",-" + oldRET + "\t\t; restore return register\n");

    outWriter.format("\tldw\t$" + R_FP + ",$" + R_SP + "," + oldFP + "\t\t; restore old frame pointer\n");
    outWriter.format("\tadd\t$" + R_SP + ",$" + R_SP + "," + frameSize + "\t\t; release frame\n");
    outWriter.format("\tjr\t$%" + R_RET + "\t\t\t; return\n");
  }

  public void visit(StmList stmList) {
    Stm head;

    while (!stmList.isEmpty) {
      head = stmList.head;
      head.accept(this);
      stmList = stmList.tail;
      /*
      if (head.getClass() == CallStm.class) {
        ((CallStm) head).accept(this);

      } else if (head.getClass() == WhileStm.class) {
        ((WhileStm) head).accept(this);

      } else if (head.getClass() == IfStm.class) {
        ((IfStm) head).accept(this);

      } else if (head.getClass() == CompStm.class) {
        ((CompStm) head).accept(this);
      }*/
    }
  }

  public void visit(AssignStm assignStm) {

  }

  public void visit(CallStm callStm) {

  }

  public void visit(CompStm compStm) {

  }


  public void visit(ExpList expList) {

  }

  public void visit(IfStm ifStm) {

  }

  public void visit(OpExp opExp) {

    opExp.left.accept(this);
    freeReg++;
    if (freeReg > R_MAX) {
      throw new RuntimeException("Ausdruck zu kompliziert");
    }
    opExp.right.accept(this);

    //switch case operation

    //freeReg--


  }


  public void visit(IntExp intExp) {
    outWriter.format("\tadd\t$" + freeReg + ",$0," + intExp.val + "\t\t; intExp\n");
  }

  public void visit(VarExp varExp) {
    varExp.accept(this);
  }

  public void visit(SimpleVar simpleVar) {
    VarEntry entry = (VarEntry) globalTable.lookup(simpleVar.name);
    outWriter.format("\tadd\t$" + freeReg + ",$" + R_FP + "," + entry.offset + "\t\t; SimpleVar " + simpleVar.name.toString() + " \n");
    outWriter.format("\tldw\t$" + freeReg + ",$" + freeReg + "," + 0 + " \n");

  }

  public void visit(ArrayVar arrayVar) {

    if (freeReg + 1 > R_MAX) throw new RuntimeException("Ausdruck zu kompliziert");

    //arrayVar.var.accept(this); //freereg = 8
    freeReg++;
    arrayVar.index.accept(this);  // free 8

    SimpleVar var = (SimpleVar)arrayVar.var;
    VarEntry entry = (VarEntry)globalTable.lookup(var.name);
    ArrayType aType = (ArrayType)entry.type;
    //9

    basetypesize = node->type_t->size;
    actualarraysize = node->u.arrayVar.var->type_t->u.arrayType.size;

    outWriter.format("\tadd\t$" + freeReg + ",$" + 0 + "," +aType.size + "\n" );
    outWriter.format("\tbgeu\t$" + (freeReg - 1) + ",$" + freeReg + ",_indexError\n");
    outWriter.format("\tmul\t$" + (freeReg - 1) + ",$" + (freeReg -1) + ",$" + aType.baseType.getByteSize() + " \n" );
    outWriter.format("\tadd\t$%d,$%d,$%d\n", r, r, r+1);

  }

  public void visit(WhileStm whileStm) {

  }


  //These Methods are irrelevant for the code generation
  public void visit(NameTy nameTy) {
  }

  public void visit(ArrayTy arrayTy) {
  }

  public void visit(TypeDec typeDec) {
  }

  public void visit(ParDec parDec) {
  }

  public void visit(EmptyStm emptyStm) {
  }

  public void visit(VarDec varDec) {
  }
}
