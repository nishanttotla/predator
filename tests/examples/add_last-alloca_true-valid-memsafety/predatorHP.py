#!/usr/bin/python

import subprocess
import time
import shlex
import sys
import os
import signal
import argparse
import tempfile
import shutil

predator_root = os.path.join(os.environ["PREDATOR_ROOT"], "predator-hp")

proving_predators = None
bugfinding_predators = None
predator_bfs = None

witness_dfs_400 = None
witness_dfs_700 = None
witness_dfs_1000 = None
witness_bfs = None

def onSIGKILL(signal, frame):
  if proving_predators:
    proving_predators.kill()
  if bugfinding_predators:
    bugfinding_predators.kill()
  if predator_bfs and predator_bfs.process:
    predator_bfs.kill()
  sys.exit(0)

signal.signal(signal.SIGTERM, onSIGKILL)
signal.signal(signal.SIGINT, onSIGKILL)

class PredatorProcess:
  def __init__(self, executable, title, witness=None):
    self.executable = executable
    self.answer = None
    self.title = title
    self.process = None
    self.witness = witness

  def execute(self):
    print "About to exec", " ".join(self.executable)
    if self.process is None:
        self.process = subprocess.Popen(self.executable, stdout=subprocess.PIPE, preexec_fn=os.setpgrp)
        #out = self.process.communicate()
        #print "out is", out

  def answered(self, result):
    if not self.terminated():
      return False
    output = self.process.communicate()[0]
    if output is not None:
      self.answer = output.split()[0]
      return self.answer[:len(result)] == result

  def getAnswer(self):
    return (self.answer, self.witness)

  def terminated(self):
    return self.process.poll() is not None

  def kill(self):
    if not self.terminated():
      os.kill(-self.process.pid, signal.SIGKILL)
    self.process.wait()

class PredatorBatch:
  def __init__(self, predator_list):
    self.predators = predator_list

  def launch(self):
		try:
			for predator in self.predators:
				predator.execute()
		except Exception, e:
			print "Launch exception is ", e

  def kill(self):
    for predator in self.predators:
      predator.kill()
    self.predators = []

  def answer(self, expected):
    for predator in self.predators[:]:
      if predator.terminated():
        self.predators.remove(predator)
        if predator.answered(expected):
          return predator.getAnswer()
    return (None, None)

  def running(self):
    return len(self.predators)

if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument("--propertyfile", dest="propertyfile")
  parser.add_argument("--witness", dest="witness")
  parser.add_argument("testcase")
  args = parser.parse_args()

  witness_dfs_400 = tempfile.mkstemp()[1]
  witness_dfs_700 = tempfile.mkstemp()[1]
  witness_dfs_1000 = tempfile.mkstemp()[1]
  witness_bfs = tempfile.mkstemp()[1]

  accel_script = os.path.join(predator_root,"predator","sl_build","check-property.sh")
  print "accel_script ", accel_script
  dfs_script = os.path.join(predator_root,"predator-dfs", "sl_build","check-property.sh")
  print "dfs_script ", dfs_script
  bfs_script = os.path.join(predator_root,"predator-bfs", "sl_build","check-property.sh")
  print "bfs_script ", bfs_script

  predator_accelerated = PredatorProcess(shlex.split(accel_script + " --trace=/dev/null --propertyfile %s -- %s -m32" % (args.propertyfile, args.testcase)), "Accelerated")
  predator_dfs_400 = PredatorProcess(shlex.split(dfs_script + " --trace=/dev/null --propertyfile %s --xmltrace %s --depth 400 -- %s -m32" % (args.propertyfile, witness_dfs_400, args.testcase)), "DFS 400", witness_dfs_400)
  predator_dfs_700 = PredatorProcess(shlex.split(dfs_script + " --trace=/dev/null --propertyfile %s --xmltrace %s --depth 700 -- %s -m32" % ( args.propertyfile, witness_dfs_700, args.testcase)), "DFS 700", witness_dfs_700)
  predator_dfs_1000 = PredatorProcess(shlex.split(dfs_script + " --trace=/dev/null --propertyfile %s --xmltrace %s --depth 1000 -- %s -m32" % (args.propertyfile, witness_dfs_1000, args.testcase)), "DFS 1000", witness_dfs_1000)
  predator_bfs = PredatorProcess(shlex.split(bfs_script + " --trace=/dev/null --propertyfile %s --xmltrace %s -- %s -m32" % (args.propertyfile, witness_bfs, args.testcase)), "BFS", witness_bfs)

  proving_predators = PredatorBatch([predator_accelerated])
  bugfinding_predators = PredatorBatch([predator_dfs_400, predator_dfs_700, predator_dfs_1000])

  proving_predators.launch()
  bugfinding_predators.launch()

  answer = None
  while True:
    answer, witness = proving_predators.answer("TRUE")
    if answer is None:
      answer, witness = bugfinding_predators.answer("FALSE")

    if answer:
      print(answer)
      if witness and answer[0] == "F":
        shutil.copyfile(witness, args.witness)
      break

    if (proving_predators.running() + bugfinding_predators.running()) == 0:
      break

    time.sleep(1)

  if answer is None and (not bugfinding_predators.running()) and False not in [ True if predator.getAnswer()[0] == "TRUE" else False for predator in bugfinding_predators.predators ]:
    predator_bfs.execute()
    while True:
      if predator_bfs.terminated():
        predator_bfs.answered("TRUE")
        answer, witness = predator_bfs.getAnswer()
        if witness and answer[0] == "F":
          shutil.copyfile(witness, args.witness)
        print(answer)
        break
      time.sleep(1)

  proving_predators.kill()
  bugfinding_predators.kill()

  if answer is None:
    print "UNKNOWN"
  os.unlink(witness_bfs)
  os.unlink(witness_dfs_400)
  os.unlink(witness_dfs_700)
  os.unlink(witness_dfs_1000)
