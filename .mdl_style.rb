# SPDX-FileCopyrightText: 2021 - 2024 Robin Vobruba <hoijui.quaero@gmail.com>
#
# SPDX-License-Identifier: Unlicense

# Enforce the style guide at https://cirosantilli.com/markdown-style-guide
all

rule 'MD003', :style => :atx
#rule 'MD004', :style => :dash
#rule 'MD004', :style => :asterisk
#rule 'MD004', :style => :consistent
#rule 'MD007', :indent => 4
rule 'MD013', :tables => false
rule 'MD029', :style => 'ordered'
#rule 'MD030', :ul_multi => 3, :ol_multi => 2
rule 'MD035', :style => '---'

# First header should be a top level header
# - We have the title in the YAML meta-data,
#   so we can use headers however we want
exclude_rule 'MD002'
# No hard tabs
# - We use tabs in the sources,
#   and thus also in the excerpts from the sources
exclude_rule 'MD010'
# Multiple top level headers in the same document
# - We have the title in the YAML meta-data,
#   so we can legitimately use level 1 headers for sections
exclude_rule 'MD025'
# Trailing punctuation in header
exclude_rule 'MD026'
# First line in file should be a top level header
# - The README has the title as a big ASCII-art
exclude_rule 'MD041'
