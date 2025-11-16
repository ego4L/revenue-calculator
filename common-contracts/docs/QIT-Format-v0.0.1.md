# QIT Format Specification


Version 0.0.1

This document describes the **QIT (Quick Input Transactions)** format — a compact, human-friendly text format for recording financial transactions in chronological order with minimal user effort.

## Overview

- **File type**: Plain text
- **Currency**: USD
- **Amounts**: Specified in **cents** (e.g., 5000 = $50.00)
- **Purpose**: To quickly log transactions using simple rules and minimal keystrokes
- **Chronology**: Entries are assumed to be listed in chronological order

---

## Syntax Rules

### 1. Year
- A line containing **exactly four digits** is interpreted as a **year**.
- Example:
  ```
  2025
  ```

### 2. Month
- A line containing **two digits preceded by a tab character** (`\t`) denotes a **month**.
- Example:
  ```
  	06
  ```

### 3. Day
- A line with **just two digits** denotes the **day of the month**.
- Example:
  ```
  26
  ```

---

## Transaction Entries

### General Format

```
HHMM [optional: SYMBOL QUANTITY] +/-AMOUNT
```

- **HHMM** — Time in 24-hour format (e.g., `1720` = 17:20 or 5:20 PM)
- **SYMBOL** — (Optional) Ticker symbol of the stock or asset (e.g., `amd`, `lmt`)
- **QUANTITY** — (Optional) Quantity of the asset traded
- **AMOUNT** — Signed transaction value in **cents**
  - **+** indicates **deposit** or **income**
  - **−** indicates **withdrawal** or **expense**

---

## Examples

### 1. Deposit
```
1720 +5000
```
→ At 17:20, 5000 cents ($50.00) were **deposited** into the brokerage account.

---

### 2. Withdrawal
```
1720 -5000
```
→ At 17:20, 5000 cents ($50.00) were **withdrawn** from the account.

---

### 3. Stock Purchase
```
1722 amd 1.01 -4610
```
→ At 17:22, **1.01 shares of AMD** were **purchased** for **4610 cents**.

---

### 4. Stock Sale
```
1722 amd 1.01 +4610
```
→ At 17:22, **1.01 shares of AMD** were **sold** for **4610 cents**.

---


### 6. Dividend or Payout
```
1210 lmt 0 +255
```
→ At 12:10, **255 cents** were received (e.g., as dividends from holding **LMT**).

---

## Commission Fees

Commission fees represent costs associated with maintaining or operating the brokerage account.

### Format

```
HHMM COMMISSION -AMOUNT
```

- **HHMM** — Time in 24-hour format (e.g., `1720` = 17:20 or 5:20 PM)
- **AMOUNT** — Fee amount in cents (always negative)

### Example

```
0108 COMMISSION -002
```

→ At 01:18, a **commission fee** of **2 cents** was charged to the account.


## Notes

- All values are in **USD cents**.
- Asset symbols are lowercase by convention, but this is not enforced.
- Times should be unique or monotonically increasing within a day to preserve order.

