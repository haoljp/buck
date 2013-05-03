/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.rules;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.shell.AidlCommand;
import com.facebook.buck.shell.Command;
import com.facebook.buck.command.io.MkdirCommand;
import com.facebook.buck.util.BuckConstant;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Build rule for generating a .java file from an .aidl file. Example:
 * <pre>
 * # This will generate IOrcaService.java in the genfiles directory.
 * gen_aidl(
 *   name = 'orcaservice',
 *   aidl = 'IOrcaService.aidl',
 * )
 *
 * # The gen() function flags the input as a file that can be found in the genfiles directory.
 * android_library(
 *   name = 'server',
 *   srcs = glob(['*.java']) + [gen('IOrcaService.java')],
 *   deps = [
 *     '//first-party/orca/lib-base:lib-base',
 *   ],
 * )
 * </pre>
 */
public class GenAidlRule extends AbstractCachingBuildRule {

  private final String aidlFilePath;
  private final String importPath;

  private GenAidlRule(BuildRuleParams buildRuleParams,
      String aidlFilePath,
      String importPath) {
    super(buildRuleParams);
    this.aidlFilePath = Preconditions.checkNotNull(aidlFilePath);
    this.importPath = Preconditions.checkNotNull(importPath);
  }

  @Override
  public BuildRuleType getType() {
    return BuildRuleType.GEN_AIDL;
  }

  @Override
  public boolean isAndroidRule() {
    return true;
  }

  @Override
  protected RuleKey.Builder ruleKeyBuilder() {
    return super.ruleKeyBuilder()
        .set("aidlFilePath", aidlFilePath)
        .set("importPath", importPath);
  }

  @Override
  protected ImmutableList<String> getInputsToCompareToOutput(BuildContext context) {
    return ImmutableList.of(aidlFilePath);
  }

  @Override
  protected List<Command> buildInternal(BuildContext context)
      throws IOException {
    ImmutableList.Builder<Command> commands = ImmutableList.builder();

    String destinationDirectory = String.format("%s/%s", BuckConstant.GEN_DIR, importPath);
    commands.add(new MkdirCommand(destinationDirectory));

    AidlCommand command = new AidlCommand(aidlFilePath,
        ImmutableSet.of(importPath),
        destinationDirectory);
    commands.add(command);

    return commands.build();
  }

  public static Builder newGenAidlRuleBuilder() {
    return new Builder();
  }

  public static class Builder extends AbstractBuildRuleBuilder {

    private String aidl;

    private String importPath;

    private Builder() {}

    @Override
    public GenAidlRule build(Map<String, BuildRule> buildRuleIndex) {
      return new GenAidlRule(createBuildRuleParams(buildRuleIndex), aidl, importPath);
    }

    @Override
    public Builder setBuildTarget(BuildTarget buildTarget) {
      super.setBuildTarget(buildTarget);
      return this;
    }

    public Builder setAidl(String aidl) {
      this.aidl = aidl;
      return this;
    }

    public Builder setImportPath(String importPath) {
      this.importPath = importPath;
      return this;
    }
  }
}
